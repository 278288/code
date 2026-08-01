package com.bjpowernode.config.handler;

import com.bjpowernode.result.CodeEnum;
import com.bjpowernode.result.R;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器（AOP 切面）。
 * 拦截所有标注 @RestController 的 Controller 中抛出的异常，统一返回 JSON 格式的错误信息。
 *
 * 异常匹配顺序：子类优先精确匹配 → 匹配不到则走父类 Exception.class。
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 文件上传大小超限异常（multipart 解析阶段触发，如 Excel 导入文件过大）。
     * 单独处理并返回中文提示，避免落到通用异常兜底显示英文堆栈信息。
     */
    @ExceptionHandler(value = MaxUploadSizeExceededException.class)
    public R handException(MaxUploadSizeExceededException e) {
        return R.FAIL("上传文件大小超出限制（最大50MB）");
    }

    /**
     * 通用异常兜底，捕获所有未精确匹配的异常。
     * Controller 方法抛出任何未处理的异常，都会走这里，避免直接暴露 500 堆栈给前端。
     */
    @ExceptionHandler(value = Exception.class)
    public R handException(Exception e) {
        e.printStackTrace();
        return R.FAIL(e.getMessage());
    }

    /**
     * 数据库访问异常（DataAccessException 及其子类，如 SQL 错误、连接失败等）。
     */
    @ExceptionHandler(value = DataAccessException.class)
    public R handException(DataAccessException e) {
        e.printStackTrace();
        return R.FAIL(CodeEnum.DATA_ACCESS_EXCEPTION);
    }

    /**
     * 权限不足异常（方法级 @PreAuthorize 校验失败时触发）。
     */
    @ExceptionHandler(value = AccessDeniedException.class)
    public R handException(AccessDeniedException e) {
        e.printStackTrace();
        return R.FAIL(CodeEnum.ACCESS_DENIED);
    }

    /**
     * JSR-303 参数校验异常（@Valid 校验失败）。
     * 返回 HTTP 400 + 中文错误提示（多个字段错误用顿号拼接）。
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public R handException(MethodArgumentNotValidException e) {
        StringBuilder sb = new StringBuilder();
        e.getBindingResult().getFieldErrors().forEach(fieldError -> {
            if (sb.length() > 0) {
                sb.append(",");
            }
            sb.append(fieldError.getDefaultMessage());
        });
        return R.FAIL(sb.toString());
    }
}