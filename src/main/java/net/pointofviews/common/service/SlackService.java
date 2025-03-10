package net.pointofviews.common.service;

import net.pointofviews.common.slack.SlackPaymentDto;

public interface SlackService {

	void sendMessage(SlackPaymentDto dto);
}
