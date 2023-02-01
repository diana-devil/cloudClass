package com.xuecheng.base.exception;

import java.io.Serializable;

/**
 * @ClassName RestErrorResponse
 * @Date 2023/2/1 14:47
 * @Author diane
 * @Description 错误响应参数包装， 与前端约定好
 *      异常类型，用json格式返回，键为  errMessage , 值为异常信息
 * @Version 1.0
 */
public class RestErrorResponse implements Serializable {
    private String errMessage;

    public RestErrorResponse(String errMessage){
        this.errMessage= errMessage;
    }

    public String getErrMessage() {
        return errMessage;
    }

    public void setErrMessage(String errMessage) {
        this.errMessage = errMessage;
    }
}
