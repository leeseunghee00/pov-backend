package net.pointofviews.payment.dto.response;

import java.time.OffsetDateTime;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ConfirmPaymentResponse(
        String paymentKey,
        String orderId,
        int totalAmount,
		String failureCode,
        OffsetDateTime requestedAt,
        OffsetDateTime approvedAt
) {
}
