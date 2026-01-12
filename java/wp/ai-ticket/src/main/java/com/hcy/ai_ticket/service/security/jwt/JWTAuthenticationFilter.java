package com.hcy.ai_ticket.service.security.jwt;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import com.hcy.ai_ticket.util.DebugTrace;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class JWTAuthenticationFilter extends OncePerRequestFilter{
	private static final Logger LOGGER = LoggerFactory.getLogger(JWTAuthenticationFilter.class);
	private static final DebugTrace TRACE = new DebugTrace(LOGGER, LOGGER.isDebugEnabled());
	
	 @Autowired
	    private JWTUtils jwtUtils;

	    @Override
	    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
	            throws ServletException, IOException {
	        try {
	        	
	            String jwt = parseJwt(request);

	            if (jwt != null && jwtUtils.validateJwtToken(jwt)) {
	            	
	                String username = jwtUtils.getUserNameFromJwtToken(jwt);

	                MDC.put("user", username);

	                UsernamePasswordAuthenticationToken authentication = 
	                        new UsernamePasswordAuthenticationToken(username, null, null);
	                
	                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

	                SecurityContextHolder.getContext().setAuthentication(authentication);
	            }
	        } catch (Exception e) {
	        	LOGGER.error("無法設定使用者認證: {}", e.getMessage());
	        } finally {
	            filterChain.doFilter(request, response);
	            MDC.remove("user");
	        }
	    }

	    private String parseJwt(HttpServletRequest request) {
	        String headerAuth = request.getHeader("Authorization");

	        if (StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ")) {
	            return headerAuth.substring(7); 
	        }
	        return null;
	    }
}
