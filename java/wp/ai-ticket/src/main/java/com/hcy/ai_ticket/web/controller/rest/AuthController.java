package com.hcy.ai_ticket.web.controller.rest;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hcy.ai_ticket.service.security.jwt.JWTUtils;
import com.hcy.ai_ticket.service.userinfo.dto.JWTResponse;
import com.hcy.ai_ticket.service.userinfo.dto.LoginRequest;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
	
	 	@Autowired
	    private AuthenticationManager authenticationManager;

	    @Autowired
	    private JWTUtils jwtUtils;

	    @PostMapping("/login")
	    public ResponseEntity<?> authenticateUser(@RequestBody LoginRequest loginRequest) {
	        
	        Authentication authentication = authenticationManager.authenticate(
	                new UsernamePasswordAuthenticationToken(
	                        loginRequest.getUsername(), 
	                        loginRequest.getPassword()
	                )
	        );

	        SecurityContextHolder.getContext().setAuthentication(authentication);

	        String jwt = jwtUtils.generateJwtToken(loginRequest.getUsername());

	        return ResponseEntity.ok(new JWTResponse(jwt));
	    }
	    
	    @GetMapping("/getUserInfo")
	    public ResponseEntity<?> getUserName(@RequestHeader("Authorization") String authHeader) {
	        try {
	            String token = authHeader.substring(7);
	            String userName = jwtUtils.getUserNameFromJwtToken(token);
	            
	            return ResponseEntity.ok(Map.of("user", userName));
	        } catch (Exception e) {
	            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid Token");
	        }
	    }
	    
	    
	    
}
