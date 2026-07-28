package com.bjpowernode.config.handler;

import com.bjpowernode.constant.Constants;
import com.bjpowernode.model.TUser;
import com.bjpowernode.result.CodeEnum;
import com.bjpowernode.result.R;
import com.bjpowernode.service.RedisService;
import com.bjpowernode.util.JSONUtils;
import com.bjpowernode.util.ResponseUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 退出登录成功处理器。
 * 从 Redis 中删除当前用户的 JWT，使后续携带该 token 的请求校验失败，实现登出。
 */
@Component
public class MyLogoutSuccessHandler implements LogoutSuccessHandler {

    @Resource
    private RedisService redisService;

    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response,
                                Authentication authentication) throws IOException, ServletException {
        TUser tUser = (TUser) authentication.getPrincipal();

        // 删除 Redis 中该用户的 JWT，token 立即失效
        redisService.removeValue(Constants.REDIS_JWT_KEY + tUser.getId());

        R result = R.OK(CodeEnum.USER_LOGOUT);
        ResponseUtils.write(response, JSONUtils.toJSON(result));
    }
}