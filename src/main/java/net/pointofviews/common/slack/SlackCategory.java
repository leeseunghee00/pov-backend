package net.pointofviews.common.slack;

import lombok.Getter;

@Getter
public enum SlackCategory {

	RETRY_PAYMENT("결제 재시도 실패"),
	CANCEL_PAYMENT("결제 취소 실패");

	final String category;

	SlackCategory(String category) {
		this.category = category;
	}
}
