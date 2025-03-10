package net.pointofviews.common.slack;

import java.time.LocalDateTime;
import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(title = "Slack 기본 메시지 정보")
public record SlackDefaultMessage(

	@Schema(name = "에러 발생 카테고리", example = "결제 재시도")
	String type,

	@Schema(name = "회원 ID", example = "1")
	UUID memberId,

	@Schema(name = "에러 발생 일시")
	LocalDateTime occurredAt,

	@Schema(name = "에러 상세 내용")
	String errorStack

) {
}
