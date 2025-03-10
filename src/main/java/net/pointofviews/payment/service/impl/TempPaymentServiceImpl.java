package net.pointofviews.payment.service.impl;

import static net.pointofviews.member.exception.MemberException.*;
import static net.pointofviews.payment.domain.OrderType.*;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.pointofviews.member.domain.Member;
import net.pointofviews.member.repository.MemberRepository;
import net.pointofviews.payment.domain.TempPayment;
import net.pointofviews.payment.dto.TempPaymentDto;
import net.pointofviews.payment.repository.TempPaymentRepository;
import net.pointofviews.payment.service.TempPaymentService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class TempPaymentServiceImpl implements TempPaymentService {

    private final TempPaymentRepository tempPaymentRepository;
    private final MemberRepository memberRepository;

    @Override
    @Transactional
    public TempPaymentDto saveTempPayment(Member loginMember, TempPaymentDto request) {

        Member member = memberRepository.findById(loginMember.getId())
                .orElseThrow(() -> memberNotFound(loginMember.getId()));

        TempPayment tempPayment = TempPayment.builder()
                .member(member)
                .type(NORMAL)
                .orderId(request.orderId())
                .amount(request.amount())
                .build();

        tempPaymentRepository.save(tempPayment);

        return request;
    }

}
