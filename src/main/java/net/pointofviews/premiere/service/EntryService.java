package net.pointofviews.premiere.service;

import net.pointofviews.member.domain.Member;
import net.pointofviews.premiere.domain.Premiere;
import net.pointofviews.premiere.dto.request.CreateEntryRequest;
import net.pointofviews.premiere.dto.request.DeleteEntryRequest;
import net.pointofviews.premiere.dto.response.CreateEntryResponse;
import net.pointofviews.premiere.dto.response.ReadMyEntryListResponse;

public interface EntryService {

    CreateEntryResponse prepareEntry(Member loginMember, Long premiereId, CreateEntryRequest request);

    void saveEntry(Member loginMember, Premiere premiere, CreateEntryRequest request, String orderId);

    void deleteEntry(Member loginMember, Long premiereId, DeleteEntryRequest request);

    ReadMyEntryListResponse findMyEntryList(Member loginMember);
}
