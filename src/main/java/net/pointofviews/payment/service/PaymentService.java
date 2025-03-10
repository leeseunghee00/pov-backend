package net.pointofviews.payment.service;

import net.pointofviews.member.domain.Member;
import net.pointofviews.payment.dto.PaymentDto;
import net.pointofviews.payment.dto.request.ConfirmPaymentRequest;

public interface PaymentService {

    PaymentDto confirmPayment(Member loginMember, ConfirmPaymentRequest request);

    void savePayment(PaymentDto dto);
}
