package com.hcy.ai_ticket.service.webSocket.dto;

import java.util.concurrent.atomic.AtomicInteger;

public class TaskProgress {

	private final AtomicInteger completedCount = new AtomicInteger(0);
	private final AtomicInteger lastSentPercent = new AtomicInteger(-1);

	public TaskProgress() {
	}

	public AtomicInteger getCompletedCount() {
		return completedCount;
	}

	public AtomicInteger getLastSentPercent() {
		return lastSentPercent;
	}

}
