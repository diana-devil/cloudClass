package com.xuecheng.ucenter.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.ucenter.mapper.XcUserMapper;
import com.xuecheng.ucenter.mapper.XcUserRoleMapper;
import com.xuecheng.ucenter.model.dto.AuthParamsDto;
import com.xuecheng.ucenter.model.dto.XcUserExt;
import com.xuecheng.ucenter.model.po.XcUser;
import com.xuecheng.ucenter.model.po.XcUserRole;
import com.xuecheng.ucenter.service.AuthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * @ClassName WxAuthServiceImpl
 * @Date 2023/2/11 19:39
 * @Author diane
 * @Description 微信登陆认证
 * @Version 1.0
 */
@Slf4j
@Service("wx_authservice")
public class WxAuthServiceImpl implements AuthService {

    @Resource
    private XcUserMapper xcUserMapper;

    @Resource
    private RestTemplate restTemplate;

    @Resource
    private XcUserRoleMapper xcUserRoleMapper;

    @Resource
    private WxAuthServiceImpl currentProxy;

    // @Value("${weixin.appid}")
    // String appid;
    // @Value("${weixin.secret}")
    // String secret;


    String appid = "appid";
    String secret = "secret";


    /**
     * 根据授权码 获取令牌-获取用户信息-并将信息存储到数据库
     * @param code 微信授权码
     * @return
     */
    public XcUser wxAuth(String code){
        // 获取access_token
        Map<String, String> token = getAccess_token(code);
        String access_token = token.get("access_token");
        String openid = token.get("openid");

        // 获取用户信息
        Map<String, String> userinfo = getUserinfo(access_token, openid);

        // 添加用户到数据库
        // 非事务方法调用事务方法，使用代理对象调用
        XcUser xcUser = currentProxy.addWxUser(userinfo);

        return xcUser;
    }


    /**
     * 根据授权码 获取令牌
     *
     * http请求方式: GET
     * https://api.weixin.qq.com/sns/oauth2/access_token?appid=APPID&secret=SECRET&code=CODE&grant_type=authorization_code
     *
     *  实例结果
     * {
     *  "access_token":"ACCESS_TOKEN",
     *  "expires_in":7200,
     *  "refresh_token":"REFRESH_TOKEN",
     *  "openid":"OPENID",
     *  "scope":"SCOPE",
     *  "unionid": "o6_bmasdasdsad6_2sgVt7hMZOPfL"
     *  }
     *
     * @param code 授权码
     * @return
     */
    private Map<String,String> getAccess_token(String code) {
        String wxUrl_template = "https://api.weixin.qq.com/sns/oauth2/access_token?appid=%s&secret=%s&code=%s&grant_type=authorization_code";
        //请求微信地址
        // key 字符串拼接 %s
        String wxUrl = String.format(wxUrl_template, appid, secret, code);

        log.info("调用微信接口申请access_token, url:{}", wxUrl);

        ResponseEntity<String> exchange = restTemplate.exchange(wxUrl, HttpMethod.POST, null, String.class);

        String result = exchange.getBody();
        log.info("调用微信接口申请access_token: 返回值:{}", result);

        return JSON.parseObject(result, Map.class);

    }


    /**
     * 携带令牌请求用户信息 -(UnionID机制)
     *
     * http请求方式: GET
     * https://api.weixin.qq.com/sns/userinfo?access_token=ACCESS_TOKEN&openid=OPENID
     *
     *  获取用户信息成功后，返回的json串
     *  unionid-- 这个数据是要存到咱们自己的数据库的
     * {
     *  "openid":"OPENID",
     *  "nickname":"NICKNAME",
     *  "sex":1,
     *  "province":"PROVINCE",
     *  "city":"CITY",
     *  "country":"COUNTRY",
     *  "headimgurl": "https://thirdwx.qlogo.cn/mmopen/g3MonUZtNHkdmzicIlibx6iaFqAc56vxLSUfpb6n5WKSYVY0ChQKkiaJSgQ1dZuTOgvLLrhJbERQQ4eMsv84eavHiaiceqxibJxCfHe/0",
     *  "privilege":[
     *  "PRIVILEGE1",
     *  "PRIVILEGE2"
     * ],
     * "unionid": " o6_bmasdasdsad6_2sgVt7hMZOPfL"
     *
     * }
     *
     */
    private Map<String,String> getUserinfo(String access_token,String openid) {
        // 拼接网址字符串
        String url = "https://api.weixin.qq.com/sns/userinfo?access_token=%s&openid=%s";
        String wxUrl = String.format(url, access_token, openid);
        log.info("调用微信接口申请access_token, url:{}", wxUrl);

        // 范围网址-请求用户信息
        ResponseEntity<String> response = restTemplate.exchange(wxUrl, HttpMethod.GET, null, String.class);
        String result = response.getBody();
        log.info("调用微信接口申请access_token: 返回值:{}", result);

        // 将结果转成map 并返回
        return JSON.parseObject(result, Map.class);

    }


    /**
     * 将用户信息 存到数据库中
     * @param userInfo_map 用户信息
     * @return
     */
    @Transactional
    public XcUser addWxUser(Map userInfo_map){
        String unionid = userInfo_map.get("unionid").toString();
        //根据unionid查询数据库
        XcUser xcUser = xcUserMapper.selectOne(new LambdaQueryWrapper<XcUser>().eq(XcUser::getWxUnionid, unionid));
        if(xcUser != null){
            // 该用户已经在数据库了，不需要在保存
            return xcUser;
        }
        String userId = UUID.randomUUID().toString();
        xcUser = new XcUser();
        xcUser.setId(userId);
        xcUser.setWxUnionid(unionid);
        //记录从微信得到的昵称
        xcUser.setNickname(userInfo_map.get("nickname").toString());
        xcUser.setUserpic(userInfo_map.get("headimgurl").toString());
        xcUser.setName(userInfo_map.get("nickname").toString());
        xcUser.setUsername(unionid);
        xcUser.setPassword(unionid);
        xcUser.setUtype("101001");//学生类型
        xcUser.setStatus("1");//用户状态
        xcUser.setCreateTime(LocalDateTime.now());
        xcUserMapper.insert(xcUser);

        XcUserRole xcUserRole = new XcUserRole();
        xcUserRole.setId(UUID.randomUUID().toString());
        xcUserRole.setUserId(userId);
        xcUserRole.setRoleId("17");//学生角色
        xcUserRoleMapper.insert(xcUserRole);
        return xcUser;

    }



    /**
     * 微信扫码认证，不校验验证码，不校验密码
     * @param authParamsDto 认证参数
     * @return
     */
    @Override
    public XcUserExt execute(AuthParamsDto authParamsDto) {
        // 获取账号
        String username = authParamsDto.getUsername();
        XcUser xcUser = xcUserMapper.selectOne(new LambdaQueryWrapper<XcUser>().eq(XcUser::getUsername, username));
        if (xcUser == null) {
            XueChengPlusException.exce("用户不存在！");
        }
        XcUserExt xcUserExt = new XcUserExt();
        BeanUtils.copyProperties(xcUser, xcUserExt);
        return xcUserExt;
    }
}
