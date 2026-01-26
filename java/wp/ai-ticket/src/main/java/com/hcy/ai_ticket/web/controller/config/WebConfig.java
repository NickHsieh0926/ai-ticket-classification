package com.hcy.ai_ticket.web.controller.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.hcy.ai_ticket.util.DebugTrace;
import com.hcy.ai_ticket.web.controller.interceptor.TraceIdInterceptor;

@Configuration
public class WebConfig implements WebMvcConfigurer {
	private static final Logger LOGGER = LoggerFactory.getLogger(WebConfig.class);
	private static final DebugTrace TRACE = new DebugTrace(LOGGER, LOGGER.isDebugEnabled());

    @Autowired
    private TraceIdInterceptor traceIdInterceptor;

    // CORS 設定
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("http://localhost:3000","http://frontend:3000")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // 必須包含 OPTIONS
                .allowedHeaders("*") // 允許所有 Header (包含 Authorization)
                .allowCredentials(true) // 允許攜帶 Cookie
                .maxAge(3600); // 預檢請求的快取時間（秒）
    }

    // MDC Trace 攔截器
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
    	TRACE.message(">>> [系統啟動] 正在註冊 TraceIdInterceptor...");
        registry.addInterceptor(traceIdInterceptor)
                .addPathPatterns("/**"); 
    }
}
