package net.pointofviews.common.toss;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Component;

import net.pointofviews.common.service.SlackService;
import net.pointofviews.common.slack.SlackCategory;
import net.pointofviews.common.slack.SlackDefaultMessage;
import net.pointofviews.common.slack.SlackPaymentDto;
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

    public ConfirmPaymentResponse confirmPayment(UUID memberId, ConfirmPaymentRequest request) {
        String authorization = tossProperty.base64SecretKey();

		ConfirmPaymentResponse response = tossClient.confirm(authorization, request);

		if (TossFailureCode.contains(response.failureCode())) {
			retryTemplate.execute(
				context -> tossClient.confirm(authorization, request),
				context -> {
					handlePaymentFailure(memberId, request, SlackCategory.RETRY_PAYMENT.name(), context.getLastThrowable());
					throw TossException.failPayment();
				}
			);
		}

		return response;
    }

    public void cancelPayment(String paymentKey, String cancelReason) {
        String authorization = tossProperty.base64SecretKey();

        tossClient.cancel(authorization, paymentKey, cancelReason);
    }

    private void handlePaymentFailure(
		UUID memberId,
		ConfirmPaymentRequest request,
		String type,
		Throwable throwable
	) {

        SlackDefaultMessage defaultMessage = new SlackDefaultMessage(
            type,
            memberId,
            LocalDateTime.now(),
            throwable.getMessage()
        );

		SlackPaymentDto sendMessage = new SlackPaymentDto(
			defaultMessage,
			request.paymentKey()
		);

		slackService.sendMessage(sendMessage);
	}
}
