package com.hcy.ai_ticket.service.webSocket.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageDeliveryException;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import com.hcy.ai_ticket.service.security.jwt.JWTUtils;
import com.hcy.ai_ticket.util.DebugTrace;
import com.hcy.ai_ticket.web.controller.config.WebConfig;

@Configuration
@EnableWebSocketMessageBroker
@Order(Ordered.HIGHEST_PRECEDENCE + 99) 
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
	private static final Logger LOGGER = LoggerFactory.getLogger(WebConfig.class);
	private static final DebugTrace TRACE = new DebugTrace(LOGGER, LOGGER.isDebugEnabled());
	
    @Autowired
    private JWTUtils jwtUtils; 
	
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic");
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws-ticket")
                .setAllowedOriginPatterns("*") 
                .withSockJS(); 
    }
    
    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

                if (StompCommand.CONNECT.equals(accessor.getCommand())) {
                    String bearerToken = accessor.getFirstNativeHeader("Authorization");

                    if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
                        String token = bearerToken.substring(7);
                        try {
                            if (jwtUtils.validateJwtToken(token)) {
                                UsernamePasswordAuthenticationToken auth = jwtUtils.getAuthentication(token);
                                accessor.setUser(auth); 
                                LOGGER.info("WebSocket 驗證成功，用戶: {}", auth.getName());
                            }else {
                                LOGGER.warn("WebSocket Token 驗證無效");
                                throw new MessageDeliveryException("Token 已失效");
                            }
                        } catch (Exception e) {
                        	LOGGER.error("WebSocket JWT 驗證失敗: {}", e.getMessage());
                            throw new MessageDeliveryException("身分驗證失敗，連線終止");
                        }
                    } else {
                    	LOGGER.warn("WebSocket 連線缺少 Token");
                        throw new MessageDeliveryException("未經授權的連線");
                    }
                }
                return message;
            }
        });
    }

}
