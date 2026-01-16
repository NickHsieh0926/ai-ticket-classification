package com.hcy.ai_ticket.service.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpStatusCodeException;

import com.hcy.ai_ticket.service.exception.dto.ErrorResponse;
import com.hcy.ai_ticket.util.DebugTrace;

@RestControllerAdvice
public class GlobalExceptionHandler {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);
	private static final DebugTrace TRACE = new DebugTrace(LOGGER, LOGGER.isDebugEnabled());
	
	@ExceptionHandler(HttpStatusCodeException.class)
	public ResponseEntity<ErrorResponse> handleHttpStatusCodeException(HttpStatusCodeException ex) {
		String traceId = MDC.get("traceId");

		LOGGER.error("AI 服務呼叫失敗 - Status: {}, Body: {}", ex.getStatusCode(), ex.getResponseBodyAsString());

		ErrorResponse error = new ErrorResponse(ex.getStatusCode().value(), "AI 預測服務異常，請稍後再試", traceId);

		return new ResponseEntity<>(error, ex.getStatusCode());
	}

	@ExceptionHandler(org.springframework.security.core.AuthenticationException.class)
	public ResponseEntity<ErrorResponse> handleAuthException(Exception ex) {
		
		ErrorResponse error = new ErrorResponse(HttpStatus.UNAUTHORIZED.value(), "帳號或密碼錯誤", MDC.get("traceId"));
		
		return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorResponse> handleGeneralException(Exception ex) {
		String traceId = MDC.get("traceId");

		LOGGER.error("系統發生未預期錯誤: ", ex);

		ErrorResponse error = new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), "系統內部發生錯誤，請提供 TraceID 供管理員查詢",
				traceId);

		return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
	}
}
