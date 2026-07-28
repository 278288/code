package com.bjpowernode.config;

import com.bjpowernode.config.filter.TokenVerifyFilter;
import com.bjpowernode.config.handler.MyAccessDeniedHandler;
import com.bjpowernode.config.handler.MyAuthenticationFailureHandler;
import com.bjpowernode.config.handler.MyAuthenticationSuccessHandler;
import com.bjpowernode.config.handler.MyLogoutSuccessHandler;
import com.bjpowernode.constant.Constants;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import com.bjpowernode.config.filter.LoginValidationFilter;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import java.util.Arrays;

/**
 * Spring Security 核心安全配置。
 *
 * 验证链路：
 *   1. LoginValidationFilter  —— 拦截 POST /api/login，校验入参 loginAct、loginPwd 不为空
 *   2. UsernamePasswordAuthenticationFilter —— 表单登录认证（loginProcessingUrl: /api/login）
 *   3. TokenVerifyFilter —— 对其他请求校验 JWT + Redis 中的 token 是否有效
 *
 * 前后端分离，禁用 Session（STATELESS），用 JWT + Redis 实现无状态认证。
 * 开启 @EnableMethodSecurity 支持方法级 @PreAuthorize 权限控制。
 */
@EnableMethodSecurity
@Configuration
public class SecurityConfig {

    @Resource
    private MyAuthenticationSuccessHandler myAuthenticationSuccessHandler;

    @Resource
    private MyAuthenticationFailureHandler myAuthenticationFailureHandler;

    @Resource
    private MyLogoutSuccessHandler myLogoutSuccessHandler;

    @Resource
    private MyAccessDeniedHandler myAccessDeniedHandler;

    @Resource
    private TokenVerifyFilter tokenVerifyFilter;

    @Resource
    private LoginValidationFilter loginValidationFilter;

    /**
     * 密码加密器。使用 BCrypt（自适应哈希），与数据库中存储的密文匹配。
     * 必须显式声明此 Bean，否则 Spring Security 会报 "PasswordEncoder mapped for id null"。
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Security 过滤器链定义。
     *
     * 请求处理顺序：
     *   POST /api/login  → formLogin 认证
     *   其他请求          → TokenVerifyFilter 校验 JWT 后放行
     *   未登录/无权限     → 返回 JSON 错误（由各 Handler 处理）
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity,
            CorsConfigurationSource configurationSource) throws Exception {
        return httpSecurity
                // 表单登录：不需要写 Controller，Spring Security 自动处理
                .formLogin((formLogin) -> {
                    formLogin.loginProcessingUrl(Constants.LOGIN_URI)
                            .usernameParameter("loginAct")
                            .passwordParameter("loginPwd")
                            .successHandler(myAuthenticationSuccessHandler)
                            .failureHandler(myAuthenticationFailureHandler);
                })

                // URL 权限：/api/login 允许匿名访问，其余需要认证
                .authorizeHttpRequests((authorize) -> {
                    authorize.requestMatchers("/api/login").permitAll()
                            .anyRequest().authenticated();
                })

                // 禁用 CSRF（前后端分离 + RESTful API 不需要）
                .csrf(AbstractHttpConfigurer::disable)

                // 跨域支持
                .cors((cors) -> {
                    cors.configurationSource(configurationSource);
                })

                // 禁用 Session（无状态，用 JWT 替代）
                .sessionManagement((session) -> {
                    session.sessionCreationPolicy(SessionCreationPolicy.STATELESS);
                })

                // 自定义过滤器：LoginValidation 在表单认证前执行，TokenVerify 在 logout 前执行
                .addFilterBefore(loginValidationFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(tokenVerifyFilter, LogoutFilter.class)

                // 退出登录
                .logout((logout) -> {
                    logout.logoutUrl("/api/logout")
                            .logoutSuccessHandler(myLogoutSuccessHandler);
                })

                // 无权限访问时触发
                .exceptionHandling((exceptionHandling) -> {
                    exceptionHandling.accessDeniedHandler(myAccessDeniedHandler);
                })

                .build();
    }

    /**
     * 跨域配置：允许所有来源、方法、请求头（开发阶段；生产环境建议限制具体域名）。
     */
    @Bean
    public CorsConfigurationSource configurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("*"));
        configuration.setAllowedMethods(Arrays.asList("*"));
        configuration.setAllowedHeaders(Arrays.asList("*"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}