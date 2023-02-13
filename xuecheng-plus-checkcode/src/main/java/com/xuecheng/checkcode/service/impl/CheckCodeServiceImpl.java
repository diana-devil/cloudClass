package com.xuecheng.checkcode.service.impl;

import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.checkcode.model.CheckCodeParamsDto;
import com.xuecheng.checkcode.model.CheckCodeResultDto;
import com.xuecheng.checkcode.service.AbstractCheckCodeService;
import com.xuecheng.checkcode.service.CheckCodeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @ClassName CheckCodeServiceImpl
 * @Date 2023/2/12 11:00
 * @Author diane
 * @Description 数字 验证码服务实现
 * @Version 1.0
 */
@Service("CheckCodeService")
@Slf4j
public class CheckCodeServiceImpl extends AbstractCheckCodeService implements CheckCodeService {

    @Resource(name="NumberLetterCheckCodeGenerator")
    @Override
    public void setCheckCodeGenerator(CheckCodeGenerator checkCodeGenerator) {
        this.checkCodeGenerator = checkCodeGenerator;
    }

    @Resource(name="UUIDKeyGenerator")
    @Override
    public void setKeyGenerator(KeyGenerator keyGenerator) {
        this.keyGenerator = keyGenerator;
    }


    @Resource(name="MemoryCheckCodeStore")
    @Override
    public void setCheckCodeStore(CheckCodeStore checkCodeStore) {
        this.checkCodeStore = checkCodeStore;
    }


    /**
     * 产生验证码
     * @param checkCodeParamsDto 生成验证码参数
     * @return
     */
    @Override
    public CheckCodeResultDto generate(CheckCodeParamsDto checkCodeParamsDto) {
        GenerateResult generate = generate(checkCodeParamsDto, 4, "checkcode:digit:", 60);
        String key = generate.getKey();
        String code = generate.getCode();
        String terminal = checkCodeParamsDto.getParam1();
        if (terminal == null) {
            XueChengPlusException.exce("请输入手机号或者邮箱地址");
        }

        // 默认直接发送成功！
        if (terminal.contains("@")) {
            // 邮箱 获取验证码
            // TODO 邮箱发送环节
           log.info("使用邮箱-{}-获取验证码！,验证码为:{}", terminal, code);
        } else {
            // 手机号 获取验证码
            // TODO 手机短信发送环节
            log.info("使用手机号-{}-获取验证码！,验证码为:{}", terminal, code);
        }
        // 封装返回参数
        CheckCodeResultDto checkCodeResultDto = new CheckCodeResultDto();
        checkCodeResultDto.setKey(key);
        checkCodeResultDto.setAliasing(null);
        return checkCodeResultDto;
    }
}
