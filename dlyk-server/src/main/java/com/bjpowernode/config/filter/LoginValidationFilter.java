package com.bjpowernode.config.filter;

import com.bjpowernode.result.R;
import com.bjpowernode.util.JSONUtils;
import com.bjpowernode.util.ResponseUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * 登录参数校验过滤器。
 * 在 Spring Security 表单认证之前执行，拦截 POST /api/login，
 * 校验 loginAct 和 loginPwd 非空，为空则直接返回 400 错误，不再走后续认证流程。
 */
@Slf4j
@Component
public class LoginValidationFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) {
        try {
            if ("/api/login".equals(request.getRequestURI())
                    && "POST".equalsIgnoreCase(request.getMethod())) {

                String loginAct = request.getParameter("loginAct");
                String loginPwd = request.getParameter("loginPwd");

                if (!StringUtils.hasText(loginAct) || !StringUtils.hasText(loginPwd)) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    response.setContentType("application/json;charset=UTF-8");
                    R result = R.FAIL("登录账号和密码不能为空");
                    ResponseUtils.write(response, JSONUtils.toJSON(result));
                    return;
                }
            }
            filterChain.doFilter(request, response);
        } catch (Exception e) {
            // 打印完整堆栈，避免静默吞掉下游的异常
            log.error("LoginValidationFilter 捕获到异常: uri={}, method={}",
                    request.getRequestURI(), request.getMethod(), e);
            throw new RuntimeException(e);
        }
    }
}