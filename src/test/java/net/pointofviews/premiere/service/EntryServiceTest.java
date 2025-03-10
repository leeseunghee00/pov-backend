package net.pointofviews.premiere.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import net.pointofviews.member.domain.Member;
import net.pointofviews.member.repository.MemberRepository;
import net.pointofviews.premiere.domain.Entry;
import net.pointofviews.premiere.domain.Premiere;
import net.pointofviews.premiere.dto.request.CreateEntryRequest;
import net.pointofviews.premiere.dto.response.CreateEntryResponse;
import net.pointofviews.premiere.repository.EntryRepository;
import net.pointofviews.premiere.repository.PremiereRepository;
import net.pointofviews.premiere.service.impl.EntryServiceImpl;

@ExtendWith(MockitoExtension.class)
class EntryServiceTest {

    @InjectMocks
    private EntryServiceImpl entryService;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private PremiereRepository premiereRepository;

    @Mock
    private EntryRepository entryRepository;

	@Nested
	class PrepareEntry {

		@Nested
		class Success {

			@Test
			void 응모_전_세부사항_검증_후_orderId_반환() {
			    // given -- 테스트의 상태 설정
				Member member = mock(Member.class);
				Premiere premiere = mock(Premiere.class);

				given(memberRepository.findById(any())).willReturn(Optional.of(member));
				given(premiereRepository.findById(any())).willReturn(Optional.of(premiere));
				given(entryRepository.existsEntryByMemberIdAndPremiereId(any(), anyLong())).willReturn(false);

				String orderId = UUID.randomUUID() + "_" + 1L;
				CreateEntryResponse response = new CreateEntryResponse(orderId);

			    // when -- 테스트하고자 하는 행동
				CreateEntryResponse result = entryService.prepareEntry(member, 1L, mock(CreateEntryRequest.class));

				// then -- 예상되는 변화 및 결과
				assertThat(result.orderId().split("_")[1]).isEqualTo(response.orderId().split("_")[1]);
			}
		}
	}

    @Nested
    class SaveEntry {

        @Nested
		class Success {

			@Test
			void 결제요청_성공_후_응모_저장() {
			    // given -- 테스트의 상태 설정
			    Member member = mock(Member.class);
				Premiere premiere = mock(Premiere.class);
				String orderId = "orderId_1";

				CreateEntryRequest request = new CreateEntryRequest(1, 50000);

				// when -- 테스트하고자 하는 행동
				entryService.saveEntry(member, premiere, request, orderId);

			    // then -- 예상되는 변화 및 결과
				ArgumentCaptor<Entry> captor = ArgumentCaptor.forClass(Entry.class);
				verify(entryRepository, times(1)).save(captor.capture());
			}
		}
    }
}
