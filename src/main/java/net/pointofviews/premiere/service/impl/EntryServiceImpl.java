package net.pointofviews.premiere.service.impl;

import static net.pointofviews.member.exception.MemberException.*;
import static net.pointofviews.premiere.exception.EntryException.*;
import static net.pointofviews.premiere.exception.PremiereException.*;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.pointofviews.member.domain.Member;
import net.pointofviews.member.repository.MemberRepository;
import net.pointofviews.payment.repository.TempPaymentRepository;
import net.pointofviews.premiere.domain.Entry;
import net.pointofviews.premiere.domain.Premiere;
import net.pointofviews.premiere.dto.request.CreateEntryRequest;
import net.pointofviews.premiere.dto.request.DeleteEntryRequest;
import net.pointofviews.premiere.dto.response.CreateEntryResponse;
import net.pointofviews.premiere.dto.response.ReadEntryResponse;
import net.pointofviews.premiere.dto.response.ReadMyEntryListResponse;
import net.pointofviews.premiere.exception.EntryException;
import net.pointofviews.premiere.repository.EntryRepository;
import net.pointofviews.premiere.repository.PremiereRepository;
import net.pointofviews.premiere.service.EntryService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EntryServiceImpl implements EntryService {

    private final EntryRepository entryRepository;
    private final PremiereRepository premiereRepository;
    private final MemberRepository memberRepository;
    private final TempPaymentRepository tempPaymentRepository;
    private final StringRedisTemplate redisTemplate;

    @Override
    public CreateEntryResponse prepareEntry(Member loginMember, Long premiereId, CreateEntryRequest request) {

        Member member = memberRepository.findById(loginMember.getId())
                .orElseThrow(() -> memberNotFound(loginMember.getId()));

        if (premiereRepository.findById(premiereId).isEmpty()) {
            throw premiereNotFound(premiereId);
        }

        if (entryRepository.existsEntryByMemberIdAndPremiereId(member.getId(), premiereId)) {
            throw duplicateEntry();
        }

        String orderId = UUID.randomUUID() + "_" + premiereId;
        String idempotencyKey = "IDEMPOTENCY:" + UUID.randomUUID().toString();

        redisTemplate.opsForValue().set(idempotencyKey, orderId, Duration.ofMinutes(10));

        return new CreateEntryResponse(orderId, idempotencyKey);
    }

    @Override
    @Transactional
    public void saveEntry(Member loginMember, Premiere premiere, CreateEntryRequest request, String orderId) {

        Entry entry = Entry.builder()
                .amount(request.amount())
                .quantity(request.quantity())
                .orderId(orderId)
                .member(loginMember)
                .premiere(premiere)
                .build();

        entryRepository.save(entry);
    }

    @Override
    @Transactional
    public void deleteEntry(Member loginMember, Long premiereId, DeleteEntryRequest request) {

        Member member = memberRepository.findById(loginMember.getId())
                .orElseThrow(() -> memberNotFound(loginMember.getId()));

        if (premiereRepository.findById(premiereId).isEmpty()) {
            throw premiereNotFound(premiereId);
        }

        Entry entry = entryRepository.findEntryByOrderId(request.orderId())
                .orElseThrow(EntryException::entryNotFound);

        if (!entry.getMember().getId().equals(member.getId())) {
            log.warn("[응모오류] 응모자 불일치 - Entry 회원 ID: {}, 삭제 요청한 회원 ID: {}", entry.getMember().getId(), member.getId());
            throw unauthorizedEntry();
        }

        entryRepository.delete(entry);

        tempPaymentRepository.findByOrderId(request.orderId())
                .ifPresent(tempPaymentRepository::delete);

    }

    @Override
    public ReadMyEntryListResponse findMyEntryList(Member loginMember) {

        Member member = memberRepository.findById(loginMember.getId())
                .orElseThrow(() -> memberNotFound(loginMember.getId()));

        List<ReadEntryResponse> entryList = entryRepository.findAllByMemberId(member.getId());

        return new ReadMyEntryListResponse(entryList);
    }
}
