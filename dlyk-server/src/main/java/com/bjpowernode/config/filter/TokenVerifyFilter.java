package com.bjpowernode.config.filter;


import com.bjpowernode.constant.Constants;
import com.bjpowernode.model.TUser;
import com.bjpowernode.result.CodeEnum;
import com.bjpowernode.result.R;
import com.bjpowernode.service.RedisService;
import com.bjpowernode.util.JSONUtils;
import com.bjpowernode.util.JWTUtils;
import com.bjpowernode.util.ResponseUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.concurrent.TimeUnit;


/**
 * JWT Token 验证过滤器。
 *
 * 验证流程：
 *   1. 登录请求（/api/login）直接放行，因为此时还没有 token
 *   2. Excel 导出请求从 URL 参数中取 token，其他请求从 Authorization 请求头取
 *   3. 校验 token 是否存在 → 是否被篡改 → Redis 中是否存在 → 是否匹配
 *   4. 全部通过后，将用户信息写入 SecurityContext，标记为已登录
 *   5. 异步刷新 Redis 中 token 的过期时间（线程池执行，不阻塞主请求）
 */
@Slf4j
@Component
public class TokenVerifyFilter extends OncePerRequestFilter {

    @Resource
    private RedisService redisService;

    /** 框架预置的线程池，用于异步刷新 token 过期时间 */
    @Resource
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        if (request.getRequestURI().equals(Constants.LOGIN_URI)) {
            filterChain.doFilter(request, response);
            return;
        }

        // 获取 token：导出请求从参数取，其他请求从请求头取
        String token = null;
        if (request.getRequestURI().equals(Constants.EXPORT_EXCEL_URI)) {
            token = request.getParameter("Authorization");
        } else {
            token = request.getHeader("Authorization");
        }

        if (!StringUtils.hasText(token)) {
            R result = R.FAIL(CodeEnum.TOKEN_IS_EMPTY);
            ResponseUtils.write(response, JSONUtils.toJSON(result));
            return;
        }

        if (!JWTUtils.verifyJWT(token)) {
            R result = R.FAIL(CodeEnum.TOKEN_IS_ERROR);
            ResponseUtils.write(response, JSONUtils.toJSON(result));
            return;
        }

        TUser tUser = JWTUtils.parseUserFromJWT(token);

        // 防御：用户 ID 为空（JWT 解析异常或数据损坏）
        if (tUser == null || tUser.getId() == null) {
            log.warn("JWT 解析失败：用户信息为空 or ID 为 null，uri={}", request.getRequestURI());
            R result = R.FAIL(CodeEnum.TOKEN_IS_ERROR);
            ResponseUtils.write(response, JSONUtils.toJSON(result));
            return;
        }

        String redisKey = Constants.REDIS_JWT_KEY + tUser.getId();
        String redisToken = (String) redisService.getValue(redisKey);

        if (!StringUtils.hasText(redisToken)) {
            R result = R.FAIL(CodeEnum.TOKEN_IS_EXPIRED);
            ResponseUtils.write(response, JSONUtils.toJSON(result));
            return;
        }

        if (!token.equals(redisToken)) {
            R result = R.FAIL(CodeEnum.TOKEN_IS_NONE_MATCH);
            ResponseUtils.write(response, JSONUtils.toJSON(result));
            return;
        }

        // 写入 SecurityContext
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(tUser, tUser.getLoginPwd(), tUser.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);

        // 异步刷新 token 过期时间
        threadPoolTaskExecutor.execute(() -> {
            String rememberMe = request.getHeader("rememberMe");
            if (Boolean.parseBoolean(rememberMe)) {
                redisService.expire(redisKey, Constants.EXPIRE_TIME, TimeUnit.SECONDS);
            } else {
                redisService.expire(redisKey, Constants.DEFAULT_EXPIRE_TIME, TimeUnit.SECONDS);
            }
        });

        filterChain.doFilter(request, response);
    }
}