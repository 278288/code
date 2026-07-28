package com.bjpowernode.config;

import com.bjpowernode.util.JWTUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;

/**
 * JWT 密钥配置类。
 * 启动时从 application.yml 读取 jwt.secret，注入到 JWTUtils 工具类中，
 * 避免密钥硬编码，便于多环境部署时通过环境变量覆盖。
 */
@Configuration
public class JwtConfig {

    /** 从配置文件读取 JWT 签名密钥，支持环境变量覆盖（） */
    @Value("")
    private String secret;

    /**
     * 在 Bean 初始化完成后，将密钥注入 JWTUtils。
     * 必须在 JWTUtils 被调用之前执行，否则签名和验证会失败。
     */
    @PostConstruct
    public void init() {
        JWTUtils.setSecret(secret);
    }
}