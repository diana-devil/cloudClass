package com.xuecheng.content.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xuecheng.base.exception.CommonError;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.content.config.MultipartSupportConfig;
import com.xuecheng.content.feignclient.MediaServiceClient.MediaServiceClient;
import com.xuecheng.content.feignclient.SearchServiceClient.SearchServiceClient;
import com.xuecheng.content.feignclient.model.CourseIndex;
import com.xuecheng.content.mapper.CourseMarketMapper;
import com.xuecheng.content.mapper.CoursePublishMapper;
import com.xuecheng.content.mapper.CoursePublishPreMapper;
import com.xuecheng.content.model.dto.CourseBaseInfoDto;
import com.xuecheng.content.model.dto.CoursePreviewDto;
import com.xuecheng.content.model.dto.TeachplanDto;
import com.xuecheng.content.model.po.*;
import com.xuecheng.content.service.*;
import com.xuecheng.messagesdk.model.po.MqMessage;
import com.xuecheng.messagesdk.service.MqMessageService;
import freemarker.template.Configuration;
import freemarker.template.Template;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.util.ClassUtils;
import org.springframework.util.ResourceUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.*;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.xuecheng.base.constants.DataDictionary.*;
import static com.xuecheng.base.constants.SystemConstants.MESSAGE_TYPE_COURSE;

/**
* @author 凉冰
* @description 针对表【course_publish(课程发布)】的数据库操作Service实现
* @createDate 2023-01-31 09:55:38
*/
@Service
@Slf4j
public class CoursePublishServiceImpl extends ServiceImpl<CoursePublishMapper, CoursePublish>
    implements CoursePublishService {

    @Resource
    private CourseBaseService courseBaseService;

    @Resource
    private TeachplanService teachplanService;

    @Resource
    private CourseTeacherService courseTeacherService;

    @Resource
    private CoursePublishPreMapper coursePublishPreMapper;

    @Resource
    private CoursePublishPreService coursePublishPreService;

    @Resource
    private CourseMarketMapper courseMarketMapper;

    @Resource
    private MqMessageService mqMessageService;

    @Resource
    private MediaServiceClient mediaServiceClient;

    @Resource
    private SearchServiceClient searchServiceClient;


    /**
     * 根据课程id 查询 所有课程相关信息
     * @param courseId 课程id
     * @return
     */
    @Override
    public CoursePreviewDto getCoursePreviewInfo(Long courseId) {
        // 1. 查询课程基本信息 + 营销信息
        CourseBaseInfoDto courseBaseInfo = courseBaseService.getCourseBaseInfo(courseId);
        // 2. 查询课程计划信息
        List<TeachplanDto> teachplayTree = teachplanService.findTeachplanTree(courseId);
        // 3. 查询课程师资信息
        List<CourseTeacher> courseTeacher = courseTeacherService.getCourseTeacher(courseId);

        // 封装信息并返回
        CoursePreviewDto coursePreviewDto = new CoursePreviewDto();
        coursePreviewDto.setCourseBase(courseBaseInfo);
        coursePreviewDto.setTeachplans(teachplayTree);
        coursePreviewDto.setCourseTeachers(courseTeacher);
        return coursePreviewDto;
    }


    /**
     * 提交审核
     * @param companyId 公司id
     * @param courseId 课程id
     */
    @Override
    @Transactional
    public void commitAudit(Long companyId, Long courseId) {
        // 0. 查询相关信息
        CoursePreviewDto coursePreviewInfo = getCoursePreviewInfo(courseId);
        // 1. 约束检查
        CourseBaseInfoDto courseBase = coursePreviewInfo.getCourseBase();
        if (courseBase == null) {
            XueChengPlusException.exce(CommonError.UNKOWN_ERROR);
        }
        // 1.1 机构一致性
        Long companyId1 = courseBase.getCompanyId();
        if (!companyId.equals(companyId1)) {
            XueChengPlusException.exce("只有本机构人员才可以提交审核");
        }
        // 1.2 审核状态为 未提交或审核完成  才可以提交
        String auditStatus = courseBase.getAuditStatus();
        if (AUDIT_COMMITTEDED.equals(auditStatus)) {
            XueChengPlusException.exce("当前审核未完成,请稍后提交！");
        }
        // 1.3 信息录入不完整
        String pic = courseBase.getPic();
        if (StringUtils.isBlank(pic)) {
            XueChengPlusException.exce("课程图片未上传！");
        }
        List<TeachplanDto> teachplans = coursePreviewInfo.getTeachplans();
        if (teachplans == null || teachplans.size() <= 0) {
            XueChengPlusException.exce("课程计划未添加！");
        }

        // 2. 将信息 插入预发布表
        // 2.1 封装基本信息
        CoursePublishPre coursePublishPre = new CoursePublishPre();
        BeanUtils.copyProperties(courseBase, coursePublishPre);
        // 设置状态 -- 已提交
        coursePublishPre.setStatus(AUDIT_COMMITTEDED);
        //提交时间
        coursePublishPre.setCreateDate(LocalDateTime.now());
        coursePublishPre.setAuditDate(LocalDateTime.now());

        // 2.2 封装 课程计划信息 -json
        String teachplansJSON = JSON.toJSONString(teachplans);
        coursePublishPre.setTeachplan(teachplansJSON);

        // 2.3 封装 课程营销信息 -json
        CourseMarket courseMarket = courseMarketMapper.selectById(courseId);
        if (courseMarket == null) {
            XueChengPlusException.exce("营销信息为空！");
        }
        String courseMarketJSON = JSON.toJSONString(courseMarket);
        coursePublishPre.setMarket(courseMarketJSON);
        coursePublishPre.setPrice(courseMarket.getPrice());
        coursePublishPre.setOriginalPrice(courseMarket.getOriginalPrice());

        // 2.4 封装 教师信息 -json
        List<CourseTeacher> courseTeachers = coursePreviewInfo.getCourseTeachers();
        String teachersJSON = JSON.toJSONString(courseTeachers);
        coursePublishPre.setTeachers(teachersJSON);

        // 2.5 插入或者更新数据库
        boolean b1 = coursePublishPreService.saveOrUpdate(coursePublishPre);
        if (!b1) {
            XueChengPlusException.exce("更新预发布表失败！");
        }

        // 3. 更新课程基本表，状态信息
        courseBase.setAuditStatus(AUDIT_COMMITTEDED);
        boolean b = courseBaseService.updateById(courseBase);
        if (!b) {
            XueChengPlusException.exce("更新课程审核信息失败！");
        }

    }


    /**
     * 课程发布
     * @param companyId 公司id
     * @param courseId 课程id
     */
    @Override
    @Transactional
    public void coursepublish(Long companyId, Long courseId) {
        // 1. 从课程预发布表获取信息, 并进行发布校验
        CoursePublishPre coursePublishPre = coursePublishPreService.getById(courseId);
        if (coursePublishPre == null) {
            XueChengPlusException.exce("请先提交课程审核，审核通过才可以发布！");
        }
        if (!companyId.equals(coursePublishPre.getCompanyId())) {
            XueChengPlusException.exce("机构不一致，无法发布！");
        }
        if (!AUDIT_PASS.equals(coursePublishPre.getStatus())) {
            XueChengPlusException.exce("课程审核还未通过！");
        }

        // 2. 向课程发布表中插入信息，更新发布状态-已发布
        CoursePublish coursePublish = new CoursePublish();
        BeanUtils.copyProperties(coursePublishPre, coursePublish);
        coursePublish.setStatus(PUBLISH_YES);
        saveOrUpdate(coursePublish);

        // 3. 更新课程基本信息表的发布状态-已发布
        CourseBase courseBase = courseBaseService.getById(courseId);
        courseBase.setStatus(PUBLISH_YES);
        courseBaseService.updateById(courseBase);

        // 4. 删除预发布表的信息
        coursePublishPreService.removeById(courseId);

        // 5. 向消息表中插入一条记录 消息类型为 course_publish
        saveCoursePublishMessage(courseId);


    }


    /**
     * 课程静态化
     * @param courseId  课程id
     * @return 静态化文件 File
     */
    @Override
    public File generateCourseHtml(Long courseId) {

        File htmlFile = null;

        try {
            //配置freemarker
            Configuration configuration = new Configuration(Configuration.getVersion());

            //加载模板
            //选指定模板路径,classpath下templates下
            //得到classpath路径
            String classpath = this.getClass().getResource("/").getPath();
            String decode = URLDecoder.decode(classpath, "UTF-8");

            configuration.setDirectoryForTemplateLoading(new File(decode + "/templates/"));
            //设置字符编码
            configuration.setDefaultEncoding("utf-8");

            //指定模板文件名称
            Template template = configuration.getTemplate("course_template.ftl");

            //准备数据
            CoursePreviewDto coursePreviewInfo = getCoursePreviewInfo(courseId);

            Map<String, Object> map = new HashMap<>();
            map.put("model", coursePreviewInfo);

            //静态化
            //参数1：模板，参数2：数据模型
            String content = FreeMarkerTemplateUtils.processTemplateIntoString(template, map);
            //将静态化内容输出到文件中
            InputStream inputStream = IOUtils.toInputStream(content);
            // 创建临时文件
            htmlFile = File.createTempFile("course", ".html");
            log.info("课程静态化，生成静态文件:{}",htmlFile.getAbsolutePath());

            //输出流
            FileOutputStream outputStream = new FileOutputStream(htmlFile);
            IOUtils.copy(inputStream, outputStream);

        } catch (Exception e) {
            e.printStackTrace();
        }

        // 有异常了，返回的是null;  无异常返回的是正常的html文件
        return htmlFile;
    }


    /**
     * 上传课程静态化页面
     * @param courseId 课程id
     * @param file  静态化文件
     */
    @Override
    public void uploadCourseHtml(Long courseId, File file) {
        MultipartFile multipartFile = MultipartSupportConfig.getMultipartFile(file);
        mediaServiceClient.upload(multipartFile,"course",courseId + ".html");
    }

    /**
     * 创建课程索引
     * @param courseId 课程id
     * @return 成功-true
     */
    @Override
    public Boolean saveCourseIndex(long courseId) {
        // 获取课程信息,封装索引信息
        CoursePublish coursePublish = getById(courseId);
        if (coursePublish == null) {
            XueChengPlusException.exce("课程发布信息为空，无法创建索引！");
        }
        CourseIndex courseIndex = new CourseIndex();
        BeanUtils.copyProperties(coursePublish, courseIndex);

        // 调用远程服务 创建课程索引
        Boolean res = searchServiceClient.add(courseIndex);
        if (!res) {
            XueChengPlusException.exce("调用远程服务,创建课程索引失败");
        }
        return true;
    }


    /**
     * 保存记录到消息表
     * @param courseId 课程id
     */
    private void saveCoursePublishMessage(Long courseId) {
        MqMessage mqMessage = mqMessageService.
                addMessage(MESSAGE_TYPE_COURSE, String.valueOf(courseId), null, null);
        // 如果插入失败，则抛出异常
        if (mqMessage == null) {
            XueChengPlusException.exce("添加消息记录异常！");
        }
    }


    /**
     * key 路径获取方式
     * @param args
     * @throws FileNotFoundException
     * @throws UnsupportedEncodingException
     * @throws URISyntaxException
     */
    public static void main(String[] args) throws FileNotFoundException, UnsupportedEncodingException, URISyntaxException {

        // 获取当前项目资源路径名称 方式 -- 中文乱码
        String classpath = CoursePublishServiceImpl.class.getResource("/").getPath();
        String classpath2 = ClassUtils.getDefaultClassLoader().getResource("").getPath();
        String locationPath = ResourceUtils.getURL("classpath:").getPath();
        System.out.println(classpath);
        System.out.println(classpath2);
        System.out.println(locationPath);


        // 中文解码
        String decode = URLDecoder.decode(classpath, "UTF-8");
        System.out.println(decode);


        // 得到根路径的方式
        File path = new File(ResourceUtils.getURL("classpath:").getPath());
        if(!path.exists()) path = new File("");
        System.out.println("path:"+path.getAbsolutePath());


    }
}




