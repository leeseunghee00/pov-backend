package net.pointofviews.payment.service.impl;

import static net.pointofviews.member.exception.MemberException.*;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.pointofviews.common.lock.DistributeLock;
import net.pointofviews.common.toss.TossClientManager;
import net.pointofviews.member.domain.Member;
import net.pointofviews.member.repository.MemberRepository;
import net.pointofviews.payment.domain.Payment;
import net.pointofviews.payment.domain.TempPayment;
import net.pointofviews.payment.dto.PaymentDto;
import net.pointofviews.payment.dto.request.ConfirmPaymentRequest;
import net.pointofviews.payment.dto.response.ConfirmPaymentResponse;
import net.pointofviews.payment.exception.PaymentException;
import net.pointofviews.payment.repository.PaymentRepository;
import net.pointofviews.payment.repository.TempPaymentRepository;
import net.pointofviews.payment.service.PaymentService;
import net.pointofviews.payment.util.PaymentValidator;
import net.pointofviews.premiere.domain.Premiere;
import net.pointofviews.premiere.dto.request.CreateEntryRequest;
import net.pointofviews.premiere.exception.PremiereException;
import net.pointofviews.premiere.repository.PremiereRepository;
import net.pointofviews.premiere.service.EntryService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final TossClientManager tossClient;
    private final PaymentRepository paymentRepository;
    private final TempPaymentRepository tempPaymentRepository;
    private final PremiereRepository premiereRepository;
    private final MemberRepository memberRepository;
    private final EntryService entryService;
    private final PaymentValidator paymentValidator;

    @Override
    @DistributeLock(key = "#request.orderId().split(\"_\")[1]")
    public PaymentDto confirmPayment(Member loginMember, ConfirmPaymentRequest request) {

        Member member = memberRepository.findById(loginMember.getId())
                .orElseThrow(() -> memberNotFound(loginMember.getId()));

        TempPayment tempPayment = tempPaymentRepository.findByOrderId(request.orderId())
                .orElseThrow(PaymentException::tempPaymentNotFound);

        Long premierId = Long.parseLong(request.orderId().split("_")[1]);
        Premiere premiere = premiereRepository.findById(premierId)
                .orElseThrow(() -> PremiereException.premiereNotFound(premierId));

        paymentValidator.validatePayment(member, tempPayment, premiere);

        ConfirmPaymentResponse response = tossClient.confirmPayment(member.getId(), request);

        return new PaymentDto(response, member, premiere);
    }

    @Override
    @Transactional
    public void savePayment(PaymentDto dto) {

        entryService.saveEntry(
                dto.member(),
                dto.premiere(),
                new CreateEntryRequest(1, dto.confirmPayment().totalAmount()),
                dto.confirmPayment().orderId()
        );

        Payment payment = Payment.builder()
                .paymentKey(dto.confirmPayment().paymentKey())
                .orderId(dto.confirmPayment().orderId())
                .vendor("TOSS")
                .amount(dto.confirmPayment().totalAmount())
                .requestedAt(dto.confirmPayment().requestedAt().toLocalDateTime())
                .approvedAt(dto.confirmPayment().approvedAt().toLocalDateTime())
                .build();

        paymentRepository.save(payment);
    }

    @Transactional
    public void cancelPayment(String paymentKey, String cancelReason) {
        tossClient.cancelPayment(paymentKey, cancelReason);
    }

}
