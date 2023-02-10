package com.xuecheng.content.api;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

/**
 * @ClassName FreeMarkerController
 * @Date 2023/2/9 10:21
 * @Author diane
 * @Description 模板引擎技术 测试接口
 * @Version 1.0
 */
// @RestController 返回的是json数据
// @Controller 返回的是页面
@Controller
public class FreeMarkerController {

    @GetMapping("/testfreemarker")
    public ModelAndView test() {
        ModelAndView modelAndView = new ModelAndView();
        // 设置模型数据   Hello ${name}!   ${}里面的就是key  值是value
        modelAndView.addObject("name", "diana");
        // 设置模板名称  -- test.ftl  去掉后缀
        modelAndView.setViewName("test");
        return modelAndView;
    }


}
