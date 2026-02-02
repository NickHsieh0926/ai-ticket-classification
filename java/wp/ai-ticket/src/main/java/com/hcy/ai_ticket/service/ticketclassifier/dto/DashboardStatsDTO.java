package com.hcy.ai_ticket.service.ticketclassifier.dto;

import java.util.List;

public class DashboardStatsDTO {

	private List<CategoryCount> categoryStats;

	private List<Long> confidenceStats;

	public List<CategoryCount> getCategoryStats() {
		return categoryStats;
	}

	public void setCategoryStats(List<CategoryCount> categoryStats) {
		this.categoryStats = categoryStats;
	}

	public List<Long> getConfidenceStats() {
		return confidenceStats;
	}

	public void setConfidenceStats(List<Long> confidenceStats) {
		this.confidenceStats = confidenceStats;
	}

	public static class CategoryCount {
		private String name;
		private Long value;

		public CategoryCount() {
		}

		public CategoryCount(String name, Long value) {
			this.name = name;
			this.value = value;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public Long getValue() {
			return value;
		}

		public void setValue(Long value) {
			this.value = value;
		}

	}

}
