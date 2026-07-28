package com.bjpowernode.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.bjpowernode.model.TUser;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT 工具类。
 *
 * 密钥通过 {@link #setSecret} 由 JwtConfig 在启动时注入，
 * 支持从环境变量 JWT_SECRET 读取（为空时使用默认值 dY8300olWQ3345;1d<3w48）。
 */
public class JWTUtils {

    private static String SECRET = null;

    /**
     * 由 JwtConfig 在应用启动时注入。
     * 允许在测试中多次调用重新设置。
     */
    public static void setSecret(String secret) {
        // 防御：env 设为空字符串时无用默认值，避免 SecretKeySpec 抛 Empty key
        if (!StringUtils.hasText(secret)) {
            secret = "dY8300olWQ3345;1d<3w48";
        }
        SECRET = secret;
    }

    private static String getSecret() {
        // 防御：未初始化时给默认值
        if (!StringUtils.hasText(SECRET)) {
            SECRET = "dY8300olWQ3345;1d<3w48";
        }
        return SECRET;
    }

    /**
     * 生成 JWT（token）。
     *
     * @param userJSON TUser 的 JSON 字符串，作为 payload 的 user claim
     */
    public static String createJWT(String userJSON) {
        Map<String, Object> header = new HashMap<>();
        header.put("alg", "HS256");
        header.put("typ", "JWT");

        return JWT.create()
                .withHeader(header)
                .withClaim("user", userJSON)
                .sign(Algorithm.HMAC256(getSecret()));
    }

    /**
     * 验证 JWT 是否有效（签名是否正确，是否过期）。
     *
     * @param jwt 要验证的 jwt 字符串
     */
    public static Boolean verifyJWT(String jwt) {
        try {
            JWTVerifier jwtVerifier = JWT.require(Algorithm.HMAC256(getSecret())).build();
            jwtVerifier.verify(jwt);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 解析 JWT 的数据（调试用，非业务方法）。
     */
    public static void parseJWT(String jwt) {
        try {
            JWTVerifier jwtVerifier = JWT.require(Algorithm.HMAC256(getSecret())).build();
            DecodedJWT decodedJWT = jwtVerifier.verify(jwt);

            Claim nickClaim = decodedJWT.getClaim("nick");
            Claim ageClaim = decodedJWT.getClaim("age");
            Claim phoneClaim = decodedJWT.getClaim("phone");
            Claim birthDayClaim = decodedJWT.getClaim("birthDay");

            String nick = nickClaim.asString();
            int age = ageClaim.asInt();
            String phone = phoneClaim.asString();
            Date birthDay = birthDayClaim.asDate();

            System.out.println(nick + " -- " + age + " -- " + phone + " -- " + birthDay);
        } catch (TokenExpiredException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    /**
     * 从 JWT 中解析出 TUser 对象。
     */
    public static TUser parseUserFromJWT(String jwt) {
        try {
            JWTVerifier jwtVerifier = JWT.require(Algorithm.HMAC256(getSecret())).build();
            DecodedJWT decodedJWT = jwtVerifier.verify(jwt);

            Claim userClaim = decodedJWT.getClaim("user");
            String userJSON = userClaim.asString();

            return JSONUtils.toBean(userJSON, TUser.class);
        } catch (TokenExpiredException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}