package com.hcy.ai_ticket.service.webSocket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.hcy.ai_ticket.service.webSocket.dto.ProgressMessage;
import com.hcy.ai_ticket.service.webSocket.staticenum.TopicType;
import com.hcy.ai_ticket.util.DebugTrace;
import com.hcy.ai_ticket.web.controller.rest.TicketController;

@Service
public class WsProgressService {
	private static final Logger LOGGER = LoggerFactory.getLogger(TicketController.class);
	private static final DebugTrace TRACE = new DebugTrace(LOGGER, LOGGER.isDebugEnabled());
	
	private final SimpMessagingTemplate messagingTemplate;

    public WsProgressService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void push(String traceId, TopicType type, long completed, long total,
                     String label, String status) {
    	TRACE.message("WebSocket 推播 [TopicType:{}]", type);
        String topic = switch (type) {
            case PROGRESS    -> "/topic/progress/" + traceId;
            case AB_PROGRESS -> "/topic/ab-progress/" + traceId;
        };
        String percentage = String.format("%.2f", (double) completed / total * 100);
        ProgressMessage msg = new ProgressMessage(
            traceId,
            String.valueOf(completed),
            String.valueOf(total),
            percentage,
            label,
            status
        );
        messagingTemplate.convertAndSend(topic, msg);
    }
}
