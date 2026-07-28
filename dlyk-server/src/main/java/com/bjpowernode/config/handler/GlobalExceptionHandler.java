package com.bjpowernode.config.handler;

import com.bjpowernode.result.CodeEnum;
import com.bjpowernode.result.R;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 统一异常处理类，controller发生了异常，统一用该类进行处理
 *
 */
@RestControllerAdvice //aop。拦截标注了@RestController的controller中的所有方法
//@ControllerAdvice //aop。拦截标注了@Controller的controller中的所有方法
public class GlobalExceptionHandler {

    /**
     * 异常处理的方法（controller方法发生了异常，那么就使用该方法来处理）
     *
     * @return
     */
    @ExceptionHandler(value = Exception.class)
    public R handException(Exception e) {
        e.printStackTrace(); //在控制台打印异常信息
        return R.FAIL(e.getMessage());
    }

    /**
     * 异常的精确匹配，先精确匹配，匹配不到了，就找父类的异常处理
     *
     * @param e
     * @return
     */
    @ExceptionHandler(value = DataAccessException.class)
    public R handException3(DataAccessException e) {
        e.printStackTrace(); //在控制台打印异常信息
        return R.FAIL(CodeEnum.DATA_ACCESS_EXCEPTION);
    }

    /**
     * 权限不足的异常处理
     *
     * @param e
     * @return
     */
    @ExceptionHandler(value = AccessDeniedException.class)
    public R handException(AccessDeniedException e) {
        e.printStackTrace(); //在控制台打印异常信息
        return R.FAIL(CodeEnum.ACCESS_DENIED);
    }

    /**
     * 参数校验未通过异常处理，HTTP 400 + 中文错误提示
     *
     * @param e
     * @return
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST) // 400 Bad Request
    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public R handException(MethodArgumentNotValidException e) {
        StringBuilder sb = new StringBuilder();
        e.getBindingResult().getFieldErrors().forEach(fieldError -> {
            if (sb.length() > 0) {
                sb.append("；");
            }
            sb.append(fieldError.getDefaultMessage());
        });
        return R.FAIL(sb.toString());
    }
}