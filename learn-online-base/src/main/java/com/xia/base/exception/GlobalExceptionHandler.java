package com.xia.base.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(value = GlobalException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public GlobalExceptionResponse globalExceptionHandler(GlobalException e) {
        log.error("【全局异常】{}", e.getErrMessage());
        return new GlobalExceptionResponse(e.getErrMessage());
    }

    @ExceptionHandler(value = Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public GlobalExceptionResponse exceptionHandler(Exception e) {
        log.error("【全局异常】{}", e.getMessage());
        return new GlobalExceptionResponse(CommonError.UNKOWN_ERROR.getErrMessage());
    }
}
