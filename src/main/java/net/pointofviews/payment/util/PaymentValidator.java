package net.pointofviews.payment.util;

import static net.pointofviews.payment.exception.PaymentException.*;
import static net.pointofviews.premiere.exception.EntryException.*;

import org.springframework.stereotype.Component;

import net.pointofviews.member.domain.Member;
import net.pointofviews.payment.domain.TempPayment;
import net.pointofviews.premiere.domain.Premiere;
import net.pointofviews.premiere.repository.EntryRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentValidator {

	private final EntryRepository entryRepository;

	public void validatePayment(Member member, TempPayment tempPayment, Premiere premiere) {
		if (!tempPayment.getMember().getId().equals(member.getId())) {
			log.warn("[결제오류] 결제자 불일치 - TempPayment 회원 ID: {}, 현재 회원 ID: {}", tempPayment.getMember().getId(), member.getId());
			throw paymentMismatch();
		}

		if (!tempPayment.getAmount().equals(premiere.getAmount())) {
			log.warn("[결제오류] 금액 불일치 - 요청한 결제 금액: {}, 실제 결제할 금액: {}", tempPayment.getAmount(), premiere.getAmount());
			throw amountMismatch();
		}

		if (entryRepository.countEntriesByPremiereId(premiere.getId()) + 1 > premiere.getMaxQuantity()) {
			log.warn("[결제오류] 수량 초과 - 시사회 수량: {}", premiere.getAmount());
			throw quantityExceeded();
		}
	}
}
