package com.bjpowernode.config.handler;

import com.bjpowernode.constant.Constants;
import com.bjpowernode.model.TUser;
import com.bjpowernode.result.R;
import com.bjpowernode.service.RedisService;
import com.bjpowernode.util.JSONUtils;
import com.bjpowernode.util.JWTUtils;
import com.bjpowernode.util.ResponseUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * 登录成功处理器。
 *
 * 执行流程：
 *   1. 从 Authentication 中获取登录用户 TUser
 *   2. 生成 JWT（payload 为 TUser 的 JSON）
 *   3. 将 JWT 写入 Redis（key: jwt:user:{userId}）
 *   4. 根据是否勾选"记住我"设置过期时间（7 天 / 30 分钟）
 *   5. 返回 JSON，body 中包含 JWT，前端存到 sessionStorage 后续请求携带
 */
@Slf4j
@Component
public class MyAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    @Resource
    private RedisService redisService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        TUser tUser = (TUser) authentication.getPrincipal();

        // 防御：如果用户 ID 为空（数据异常），直接返回错误，避免 Redis 操作传入空 key
        if (tUser.getId() == null) {
            log.error("登录成功但用户 ID 为 null，用户信息: loginAct={}", tUser.getLoginAct());
            R result = R.FAIL("用户数据异常，请联系管理员");
            ResponseUtils.write(response, JSONUtils.toJSON(result));
            return;
        }

        // 生成 JWT
        String userJSON = JSONUtils.toJSON(tUser);
        String jwt = JWTUtils.createJWT(userJSON);

        // 写入 Redis，key 格式: dlyk:user:login:{userId}
        String redisKey = Constants.REDIS_JWT_KEY + tUser.getId();
        redisService.setValue(redisKey, jwt);

        // 设置过期时间
        String rememberMe = request.getParameter("rememberMe");
        if (Boolean.parseBoolean(rememberMe)) {
            redisService.expire(redisKey, Constants.EXPIRE_TIME, TimeUnit.SECONDS);
        } else {
            redisService.expire(redisKey, Constants.DEFAULT_EXPIRE_TIME, TimeUnit.SECONDS);
        }

        R result = R.OK(jwt);
        ResponseUtils.write(response, JSONUtils.toJSON(result));
    }
}