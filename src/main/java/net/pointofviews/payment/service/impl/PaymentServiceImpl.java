package net.pointofviews.payment.service.impl;

import static net.pointofviews.member.exception.MemberException.*;

import java.time.Duration;
import java.util.Optional;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import net.pointofviews.common.lock.DistributeLock;
import net.pointofviews.common.toss.TossClientManager;
import net.pointofviews.member.domain.Member;
import net.pointofviews.member.repository.MemberRepository;
import net.pointofviews.payment.domain.Payment;
import net.pointofviews.payment.domain.TempPayment;
import net.pointofviews.payment.dto.request.ConfirmPaymentRequest;
import net.pointofviews.payment.dto.response.ConfirmPaymentResponse;
import net.pointofviews.payment.exception.PaymentException;
import net.pointofviews.payment.repository.PaymentRepository;
import net.pointofviews.payment.repository.TempPaymentRepository;
import net.pointofviews.payment.service.PaymentService;
import net.pointofviews.payment.util.PaymentValidator;
import net.pointofviews.premiere.domain.Entry;
import net.pointofviews.premiere.domain.Premiere;
import net.pointofviews.premiere.dto.request.CreateEntryRequest;
import net.pointofviews.premiere.dto.request.DeleteEntryRequest;
import net.pointofviews.premiere.exception.PremiereException;
import net.pointofviews.premiere.repository.EntryRepository;
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
	private final StringRedisTemplate redisTemplate;
	private final EntryRepository entryRepository;

	private static final Duration IDEMPOTENCY_TTL = Duration.ofMinutes(10);

	@Override
    @DistributeLock(key = "#request.orderId().split(\"_\")[1]")
    public void confirmPayment(Member loginMember, String idempotencyKey, ConfirmPaymentRequest request) {

		if ("PROCESSED".equals(redisTemplate.opsForValue().get(idempotencyKey))) {
			log.info("이미 처리된 요청입니다. 멱등키={}", idempotencyKey);
			return;
		}

        Member member = memberRepository.findById(loginMember.getId())
                .orElseThrow(() -> memberNotFound(loginMember.getId()));

        TempPayment tempPayment = tempPaymentRepository.findByOrderId(request.orderId())
                .orElseThrow(PaymentException::tempPaymentNotFound);

        Long premierId = Long.parseLong(request.orderId().split("_")[1]);
        Premiere premiere = premiereRepository.findById(premierId)
                .orElseThrow(() -> PremiereException.premiereNotFound(premierId));

        paymentValidator.validatePayment(member, tempPayment, premiere);

        ConfirmPaymentResponse response = tossClient.confirmPayment(member.getId(), request, idempotencyKey);

		try {
			entryService.saveEntry(
				member,
				premiere,
				new CreateEntryRequest(1, response.totalAmount()),
				response.orderId()
			);

			Payment payment = Payment.builder()
				.paymentKey(response.paymentKey())
				.orderId(response.orderId())
				.vendor("TOSS")
				.amount(response.totalAmount())
				.requestedAt(response.requestedAt().toLocalDateTime())
				.approvedAt(response.approvedAt().toLocalDateTime())
				.build();

			paymentRepository.save(payment);
			redisTemplate.opsForValue().set(idempotencyKey, "PROCESSED", IDEMPOTENCY_TTL);

		} catch (Exception ex) {
			if (response != null && response.failureCode() == null) {
				cancelPayment(member, request.paymentKey(), "결제오류");
			}
		}
    }

    public void cancelPayment(Member member, String paymentKey, String cancelReason) {
        tossClient.cancelPayment(member.getId(), paymentKey, cancelReason);

		Payment payment = paymentRepository.findByPaymentKey(paymentKey);

		paymentRepository.delete(payment);

		Optional<Entry> entry = entryRepository.findEntryByOrderId(payment.getOrderId());

		if (entry.isPresent() && entry.get().getOrderId().equals(payment.getOrderId())) {
			entryService.deleteEntry(
				member,
				entry.get().getPremiere().getId(),
				new DeleteEntryRequest(payment.getOrderId())
			);
		}
    }
}
