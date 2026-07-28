package com.bjpowernode.config;

import com.bjpowernode.util.JWTUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;

@Configuration
public class JwtConfig {

    @Value("${jwt.secret}")
    private String secret;

    @PostConstruct
    public void init() {
        JWTUtils.setSecret(secret);
    }
}