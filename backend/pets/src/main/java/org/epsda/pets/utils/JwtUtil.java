package org.epsda.pets.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.epsda.pets.constants.Constants;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 18483
 * Date: 2025/12/13
 * Time: 17:00
 *
 * @Author: 憨八嘎
 */
public class JwtUtil {
    // 生成密钥
    private final static String secretKeySignature = "e5sxcGbQ8qJEiJvn5ZbGHgoPp2ObPgdrwqQY8JA9Ql0=";

    private static SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretKeySignature));
    }

    public static String generateToken(Long userId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);

        return Jwts.builder()
                .claims(claims)
                .subject(userId.toString())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + Constants.TOKEN_EXPIRATION_TIME))
                .signWith(getSigningKey())
                .compact();
    }

    public static String extractEmail(String token) {
        return (String) extractClaims(token).get("email");
    }

    public static String extractUsername(String token) {
        return (String) extractClaims(token).get("username");
    }

    public static Long extractUserId(String token) {
        Object userId = extractClaims(token).get("userId");
        if (userId instanceof Number) {
            return ((Number) userId).longValue();
        }
        return null;
    }

    public static Date extractExpiration(String token) {
        return extractClaims(token).getExpiration();
    }

    private static Claims extractClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public static boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    public static boolean validateToken(String token, Long userId) {
        try {
            final Long tokenUserId = extractUserId(token);
            return (tokenUserId != null && tokenUserId.equals(userId) && !isTokenExpired(token));
        } catch (Exception e) {
            return false;
        }
    }
}
