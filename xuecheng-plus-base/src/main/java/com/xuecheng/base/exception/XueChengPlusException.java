package com.xuecheng.base.exception;

/**
 * @ClassName XueChengPlusException
 * @Date 2023/2/1 14:40
 * @Author diane
 * @Description 自定义业务异常 -- 重要
 * @Version 1.0
 */
public class XueChengPlusException extends RuntimeException{

    private static final long serialVersionUID = 5565760508056698922L;

    /**
     * 异常信息
     */
    private String errMessage;


    public XueChengPlusException() {
        super();
    }

    public XueChengPlusException(String errMessage) {
        super(errMessage);
        this.errMessage = errMessage;
    }

    public String getErrMessage() {
        return errMessage;
    }

    /**
     * 定义静态方法，方便调用
     * @param message 异常信息
     */
    public static void exce(String message) {
        throw new XueChengPlusException(message);
    }

    /**
     * 重载静态方法， 方便调用
     * @param commonError 自定义常用异常
     */
    public static void exce(CommonError commonError) {
        throw new XueChengPlusException(commonError.getErrMessage());
    }


}
