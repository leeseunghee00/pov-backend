package net.pointofviews.common.toss;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Component;

import net.pointofviews.common.service.SlackService;
import net.pointofviews.common.slack.SlackCategory;
import net.pointofviews.common.slack.SlackMessageDto;
import net.pointofviews.payment.dto.request.ConfirmPaymentRequest;
import net.pointofviews.payment.dto.response.ConfirmPaymentResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class TossClientManager {

    private final TossClient tossClient;
    private final TossProperty tossProperty;
    private final RetryTemplate retryTemplate;
    private final SlackService slackService;

    public ConfirmPaymentResponse confirmPayment(UUID memberId, ConfirmPaymentRequest request, String idempotencyKey) {
        String authorization = tossProperty.base64SecretKey();

		ConfirmPaymentResponse response = tossClient.confirm(authorization, idempotencyKey, request);

		if (TossFailureCode.contains(response.failureCode())) {
			retryTemplate.execute(
				context -> tossClient.confirm(authorization, idempotencyKey, request),
				context -> {
					slackService.sendMessage(
						new SlackMessageDto(
							SlackCategory.RETRY_PAYMENT.name(),
							Map.of(
								"회원ID", memberId,
								"paymentKey", request.paymentKey(),
								"ERROR Stack", "```" + context.getLastThrowable().getStackTrace() + "```"
							),
							LocalDateTime.now()
						)
					);
					throw TossException.failPayment();
				}
			);
		}

		return response;
    }

    public void cancelPayment(UUID memberId, String paymentKey, String cancelReason) {
        String authorization = tossProperty.base64SecretKey();

		 try {
			 tossClient.cancel(authorization, paymentKey, cancelReason);
		 } catch (Exception ex) {
			 log.error("[결제취소 오류] memberId - {}, paymentKey - {}", memberId, paymentKey);
			 slackService.sendMessage(
				 new SlackMessageDto(
					 SlackCategory.CANCEL_PAYMENT.getCategory(),
					 Map.of(
						 "회원ID", memberId,
						 "paymentKey", paymentKey,
						 "ERROR Stack", "```" + ex.getCause().getMessage() + "```"
					 ),
					 LocalDateTime.now()
				 )
			 );
		 }
    }
}
