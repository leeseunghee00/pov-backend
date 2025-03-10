package net.pointofviews.common.slack;

public enum SlackCategory {

	RETRY_PAYMENT("결제 재시도 실패");

	final String category;

	SlackCategory(String category) {
		this.category = category;
	}
}
