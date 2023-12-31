package com.xuecheng.base.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.nio.file.AccessDeniedException;
import java.sql.SQLException;
import java.util.List;

/**
 * @ClassName GlobalExceptionHandler
 * @Date 2023/2/1 14:46
 * @Author diane
 * @Description 全局异常处理器  捕获所有异常，并进行响应
 *      可针对特定异常进行响应
 * @Version 1.0
 */
@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ResponseBody  // 返回格式为 json格式
    @ExceptionHandler(XueChengPlusException.class)  //捕获 自定义的业务异常
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)  // 设置状态响应码 500
    public RestErrorResponse customException(XueChengPlusException e) {
        String errMessage = e.getErrMessage();
        log.error("【业务异常】{}",errMessage,e);
        return new RestErrorResponse(errMessage);
    }



    @ResponseBody
    @ExceptionHandler(AccessDeniedException.class)// 捕获没有权限异常
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public RestErrorResponse authException(Exception e) {

        log.error("【系统权限异常】{}",e.getMessage(),e);
        return new RestErrorResponse("没有操作此功能的权限");

        // if(e.getMessage().equals("不允许访问")){
        //     return new RestErrorResponse("没有操作此功能的权限");
        // }
        // return new RestErrorResponse(CommonError.UNKOWN_ERROR.getErrMessage());


    }



    @ResponseBody
    // 捕获 参数校验异常
    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public RestErrorResponse doValidException(MethodArgumentNotValidException argumentNotValidException) {

        BindingResult bindingResult = argumentNotValidException.getBindingResult();
        StringBuffer errMsg = new StringBuffer();

        // 拿到参数校验的错误信息
        List<FieldError> fieldErrors = bindingResult.getFieldErrors();
        // 收集错误
        fieldErrors.forEach(error -> {
            errMsg.append(error.getDefaultMessage()).append(",");
        });
        log.error(errMsg.toString());
        return new RestErrorResponse(errMsg.toString());
    }


    @ResponseBody
    // 捕获 数据库异常
    @ExceptionHandler(value = SQLException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public RestErrorResponse sqlException(SQLException sqlException) {
        String errMessage = sqlException.getMessage();
        log.error("【数据库执行异常】{}",errMessage,sqlException);
        return new RestErrorResponse(errMessage);
    }



    @ResponseBody  // 返回格式为 json格式
    @ExceptionHandler(Exception.class)  //捕获 其他异常
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)  // 设置状态响应码 500
    public RestErrorResponse exception(Exception e) {
        log.error("【全局系统异常】{}",e.getMessage(),e);

        if(e.getMessage().equals("不允许访问")){
            return new RestErrorResponse("没有操作此功能的权限");
        }

        // 响应其他异常时， 异常信息均为  UNKOWN_ERROR== “执行过程异常，请重试。”
        return new RestErrorResponse(CommonError.UNKOWN_ERROR.getErrMessage());

    }



}
