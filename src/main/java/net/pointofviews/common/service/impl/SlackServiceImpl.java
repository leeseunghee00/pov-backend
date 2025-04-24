package net.pointofviews.common.service.impl;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import net.pointofviews.common.service.SlackService;
import net.pointofviews.common.slack.SlackMessageDto;

import com.slack.api.Slack;
import com.slack.api.methods.MethodsClient;
import com.slack.api.methods.SlackApiException;
import com.slack.api.methods.request.chat.ChatPostMessageRequest;
import com.slack.api.model.Attachment;

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
	public void sendMessage(SlackMessageDto dto) {
		try {
			MethodsClient client = Slack.getInstance().methods(token);

			StringBuilder detail = new StringBuilder();
			dto.fields().forEach((key, value) -> {
				detail
					.append(key).append(": ")
					.append(value).append("\n");
			});

			Attachment attachment = Attachment.builder()
				.color("#FF0000")
				.pretext("🚨 *" + dto.category() + "*")
				.text(
					"발생일시: " + dto.occurredAt() + "\n\n" +
					"*상세 내용*" + "\n" + detail
				)
				.build();

			ChatPostMessageRequest message = ChatPostMessageRequest.builder()
				.channel(channel)
				.attachments(List.of(attachment))
				.build();

			client.chatPostMessage(message);
		} catch (IOException | SlackApiException ex) {
			log.error("[슬랙 알림 실패] ERROR: ", ex.getMessage());
		}
	}

}
