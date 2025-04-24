package net.pointofviews.common.slack;

import java.time.LocalDateTime;
import java.util.Map;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(title = "Slack 메시지 정보")
public record SlackMessageDto(

	@Schema(name = "메시지 제목(카테고리)", example = "결제 재시도 실패")
	String category,

	Map<String, Object> fields,

	@Schema(name = "발생 일시")
	LocalDateTime occurredAt

) {
}
