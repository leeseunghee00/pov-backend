package net.pointofviews.common.service.impl;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import net.pointofviews.common.service.SlackService;
import net.pointofviews.common.slack.SlackPaymentDto;

import com.slack.api.Slack;
import com.slack.api.methods.MethodsClient;
import com.slack.api.methods.SlackApiException;
import com.slack.api.methods.request.chat.ChatPostMessageRequest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class SlackServiceImpl implements SlackService {

	@Value("${slack.token}")
	private String token;

	@Value("${slack.channel}")
	private String channel;

	@Override
	public void sendMessage(SlackPaymentDto dto) {
		try {
			MethodsClient client = Slack.getInstance().methods(token);

			ChatPostMessageRequest message = ChatPostMessageRequest.builder()
				.channel(channel)
				.attachments(dto.createAttachments())
				.build();

			client.chatPostMessage(message);
		} catch (IOException | SlackApiException ex) {
			log.error("[슬랙 알림 실패] ERROR: ", ex.getMessage());
		}
	}

}
