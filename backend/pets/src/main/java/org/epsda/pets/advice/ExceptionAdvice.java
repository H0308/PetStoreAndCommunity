package org.epsda.pets.advice;

import lombok.extern.slf4j.Slf4j;
import org.epsda.pets.common.ResultWrapper;
import org.epsda.pets.constants.Constants;
import org.epsda.pets.exception.PetException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 18483
 * Date: 2025/12/08
 * Time: 10:19
 *
 * @Author: 憨八嘎
 */
@Slf4j
@ControllerAdvice
@ResponseBody // 防止出现持续返回视图导致的死循环情况
public class ExceptionAdvice {
    @ResponseStatus(value = HttpStatus.SERVICE_UNAVAILABLE)
    @ExceptionHandler(Exception.class)
    public ResultWrapper allExcetionHandler(Exception e) {
        e.printStackTrace();
        log.info("出现异常：{}", e.getMessage());
        MethodArgumentNotValidException methodValidationException = null;
        if (e instanceof MethodArgumentNotValidException) {
            methodValidationException = (MethodArgumentNotValidException) e;
        }
        String errMsg = e.getMessage();
        if (methodValidationException != null) {
            errMsg = methodValidationException.getBindingResult().getFieldError().getDefaultMessage();
        }

        return ResultWrapper.fail(Constants.SERVER_ERROR, Constants.SERVER_ERROR_MESSAGE + "：" + errMsg);
    }

    @ResponseStatus(value = HttpStatus.SERVICE_UNAVAILABLE)
    @ExceptionHandler(PetException.class)
    public ResultWrapper systemExceptionHandler(Exception e) {
        e.printStackTrace();
        log.info("出现异常：{}", e.getMessage());

        return ResultWrapper.fail(Constants.SYSTEM_ERROR, Constants.SYSTEM_ERROR_MESSAGE + "：" + e.getMessage());
    }

    @ResponseStatus(value = HttpStatus.NOT_FOUND)
    @ExceptionHandler(NoResourceFoundException.class)
    public ResultWrapper noResourceFoundException(Exception e) {
        log.info("出现异常：{}", e.getMessage());
        return ResultWrapper.fail(Constants.RESOURCE_NOT_FOUND, Constants.RESOURCE_NOT_FOUND_MESSAGE);
    }
}