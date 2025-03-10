package net.pointofviews.common.slack;

import java.util.List;

import com.slack.api.model.Attachment;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(title = "결제 실패 알림 DTO")
public record SlackPaymentDto(

	@Schema(name = "슬랙 기본 메시지 정보")
	SlackDefaultMessage defaultMessage,

	@Schema(name = "결제 ID", example = "1")
	String paymentKey

) {

	public List<Attachment> createAttachments() {

		Attachment attachment = Attachment.builder()
			.fallback("결제 실패 알림")
			.color("#FF0000")
			.pretext("🚨 *" + defaultMessage.type() + "*")
			.text("회원 ID: " + defaultMessage.memberId() +
				"\n결제 ID: " + paymentKey +
				"\n발생일시: " + defaultMessage.occurredAt() +
				"\n*ERROR Stack*: ```" + defaultMessage.errorStack() + "```")
			.build();

		return List.of(attachment);
	}
}
