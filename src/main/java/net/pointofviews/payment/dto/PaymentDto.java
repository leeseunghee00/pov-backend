package net.pointofviews.payment.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import net.pointofviews.member.domain.Member;
import net.pointofviews.payment.dto.response.ConfirmPaymentResponse;
import net.pointofviews.premiere.domain.Premiere;

public record PaymentDto(

        @Schema(description = "결제승인 API 응답 데이터")
        ConfirmPaymentResponse confirmPayment,

        @Schema(description = "결제한 회원")
        Member member,

        @Schema(description = "결제한 시사회")
        Premiere premiere
) {
}
