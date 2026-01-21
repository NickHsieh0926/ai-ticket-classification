package com.hcy.ai_ticket.service.security.jwt;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;

import javax.crypto.SecretKey;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;

import com.hcy.ai_ticket.util.DebugTrace;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;

@Component
public class JWTUtils {
	private static final Logger LOGGER = LoggerFactory.getLogger(JWTUtils.class);
	private static final DebugTrace TRACE = new DebugTrace(LOGGER, LOGGER.isDebugEnabled());

    @Value("${jwtSecret}")
    private String jwtSecret;

    @Value("${jwtExpirationMs}")
    private int jwtExpirationMs;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateJwtToken(String username) {
        return Jwts.builder()
                .setSubject(username)                 
                .setIssuedAt(new Date())              
                .setExpiration(new Date((new Date()).getTime() + jwtExpirationMs)) 
                .signWith(getSigningKey(), SignatureAlgorithm.HS256) 
                .compact();
    }

    public String getUserNameFromJwtToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    public boolean validateJwtToken(String authToken) {
        try {
            Jwts.parserBuilder().setSigningKey(getSigningKey()).build().parseClaimsJws(authToken);
            return true;
        } catch (SecurityException e) {
        	LOGGER.error("無效的 JWT 簽名: {}", e.getMessage());
        } catch (MalformedJwtException e) {
        	LOGGER.error("無效的 JWT 格式: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
        	LOGGER.error("JWT 已過期: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
        	LOGGER.error("不支援的 JWT 類型: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
        	LOGGER.error("JWT 宣告字串為空: {}", e.getMessage());
        }
        return false;
    }
    
    public UsernamePasswordAuthenticationToken getAuthentication(String token) {
        String username = getUserNameFromJwtToken(token);
        return new UsernamePasswordAuthenticationToken(username, null, new ArrayList<>());
    }
}
