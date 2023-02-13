package com.xuecheng.ucenter.feignClient.CheckCodeClient;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @ClassName CheckCodeClient
 * @Date 2023/2/11 16:31
 * @Author diane
 * @Description 使用feign远程调用验证码服务
 *      controller 参数  https://blog.csdn.net/shgh_2004/article/details/120891456
 *          -- @RequestParam  ?code=1   普通参数
 *          --  @PathVariable  /1/     路径参数
 *          -- @RequestBody  json 类型参数
 *      feign 远程调用的时候 参数前面要带上 @RequestParam
 * @Version 1.0
 */
@FeignClient(value = "checkcode", fallbackFactory = CheckCodeClientFallbackFactory.class)
public interface CheckCodeClient {


    /**
     * 校验验证码
     * @param key_code 存验证码的key
     * @param code   验证码
     * @return bool类型
     */
    @PostMapping("/checkcode/verify")
    public Boolean verify(@RequestParam("key_code") String key_code, @RequestParam("code")String code);
}
