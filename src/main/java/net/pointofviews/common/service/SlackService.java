package net.pointofviews.common.service;

import net.pointofviews.common.slack.SlackMessageDto;

public interface SlackService {

	void sendMessage(SlackMessageDto dto);
}
