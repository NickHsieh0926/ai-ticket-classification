package com.hcy.ai_ticket.service.ticketclassifier.impl.ab;

import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.hcy.ai_ticket.service.webSocket.WsProgressService;
import com.hcy.ai_ticket.service.webSocket.staticenum.TopicType;

@Service
public class AbProgressTracker {

	private static final Logger LOGGER = LoggerFactory.getLogger(AbProgressTracker.class);

	private final ConcurrentHashMap<String, int[]> progressMap = new ConcurrentHashMap<>();
	private final WsProgressService wsProgressService;

	public AbProgressTracker(WsProgressService wsProgressService) {
		this.wsProgressService = wsProgressService;
	}

	public void register(String traceId, int totalPerBatch) {
		progressMap.put(traceId, new int[] { 0, totalPerBatch * 2, 0});
		LOGGER.info("[AbTracker] 登記 traceId={}, total={}", traceId, totalPerBatch * 2);
	}

	public boolean isAbTask(String traceId) {
		return progressMap.containsKey(traceId);
	}

	public void itemComplete(String traceId, String label) {
		int[] progress = progressMap.get(traceId);
		if (progress == null)
			return;

		synchronized (progress) {
			progress[0]++;
			int completed = progress[0];
			int total = progress[1];
			int lastSentPercent = progress[2];
			int intPercent = (int) ((double) completed / total * 100);
			boolean done = completed >= total;

			if (done || intPercent > lastSentPercent) {
				progress[2] = intPercent;
				wsProgressService.push(traceId, TopicType.AB_PROGRESS, completed, total, label,
						done ? "COMPLETED" : "PROCESSING");
				LOGGER.info("[AbTracker] {}/{} ({}%)", completed, total, intPercent);
			}

			if (done) {
				progressMap.remove(traceId);
				LOGGER.info("[AbTracker] AB 全部完成");
			}
		}
	}

}
