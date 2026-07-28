package com.bjpowernode.config.handler;

import com.bjpowernode.result.CodeEnum;
import com.bjpowernode.result.R;
import com.bjpowernode.util.JSONUtils;
import com.bjpowernode.util.ResponseUtils;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 无权限访问处理器。
 * 用户已登录但缺少所需角色/权限时触发，返回 JSON 格式的权限不足错误。
 */
@Component
public class MyAccessDeniedHandler implements AccessDeniedHandler {

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException, ServletException {
        R result = R.FAIL(CodeEnum.ACCESS_DENIED);
        ResponseUtils.write(response, JSONUtils.toJSON(result));
    }
}