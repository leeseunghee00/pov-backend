package net.pointofviews.common.toss;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

import net.pointofviews.payment.dto.request.ConfirmPaymentRequest;
import net.pointofviews.payment.dto.response.ConfirmPaymentResponse;

import jakarta.validation.Valid;

@HttpExchange("https://api.tosspayments.com/v1/payments")
public interface TossClient {

    @PostExchange(url = "/confirm", contentType = MediaType.APPLICATION_JSON_VALUE)
    ConfirmPaymentResponse confirm(
            @RequestHeader(name = "Authorization") String authorization,
            @RequestHeader(name = "Idempotency-Key") String idempotencyKey,
            @RequestBody @Valid ConfirmPaymentRequest request
    );

    @PostExchange(url = "/{paymentKey}/cancel", contentType = MediaType.APPLICATION_JSON_VALUE)
    void cancel(
            @RequestHeader(name = "Authorization") String authorization,
            @PathVariable String paymentKey,
            @RequestBody String cancelPayment
    );
}
