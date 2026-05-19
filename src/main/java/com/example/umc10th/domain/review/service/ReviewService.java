package com.example.umc10th.domain.review.service;

import com.example.umc10th.domain.member.entity.Member;
import com.example.umc10th.domain.mission.entity.Store;
import com.example.umc10th.domain.mission.entity.mapping.MemberMission;
import com.example.umc10th.domain.mission.exception.MissionException;
import com.example.umc10th.domain.mission.exception.code.MissionErrorCode;
import com.example.umc10th.domain.mission.repository.MemberMissionRepository;
import com.example.umc10th.domain.mission.repository.StoreRepository;
import com.example.umc10th.domain.review.converter.ReviewConverter;
import com.example.umc10th.domain.review.dto.ReviewReqDTO;
import com.example.umc10th.domain.review.dto.ReviewResDTO;
import com.example.umc10th.domain.review.entity.Review;
import com.example.umc10th.domain.review.exception.ReviewException;
import com.example.umc10th.domain.review.exception.code.ReviewErrorCode;
import com.example.umc10th.domain.review.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final StoreRepository storeRepository;
    private final MemberMissionRepository memberMissionRepository;

    // 리뷰 작성 화면 진입용 가게 정보 조회
    public ReviewResDTO.StoreInfo getStoreInfo(Long storeId) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new ReviewException(ReviewErrorCode.STORE_NOT_FOUND));
        return ReviewConverter.toStoreInfo(store);
    }

    // 리뷰 작성
    @Transactional
    public ReviewResDTO.CreateReview createReview(Long storeId, ReviewReqDTO.CreateReview request) {
        // 1. memberMissionId로 사용자 식별
        MemberMission memberMission = memberMissionRepository.findById(request.memberMissionId())
                .orElseThrow(() -> new MissionException(MissionErrorCode.MEMBER_MISSION_NOT_FOUND));
        Member member = memberMission.getMember();

        // 2. Store 조회
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new ReviewException(ReviewErrorCode.STORE_NOT_FOUND));

        // 3. Review 생성 & 저장
        Review review = ReviewConverter.toReview(request, member, store);
        Review savedReview = reviewRepository.save(review);

        // 4. 응답 DTO 변환 (사진은 일단 빈 리스트로)
        return ReviewConverter.toCreateReview(savedReview, request.photoUrls());
    }

    // 리뷰 조회
    public ReviewResDTO.MyReviewList getMyReviews(Long memberId, Integer page, Integer size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Review> reviewPage = reviewRepository.findByMemberId(memberId, pageable);
        return ReviewConverter.toMyReviewList(reviewPage);
    }

    // 커서 기반 리뷰 조회 (ID 순 / 별점 순)
    public ReviewResDTO.MyReviewSliceList getMyReviewsByCursor(
            Long memberId, String sort, String cursor, Integer size
    ) {
        Pageable pageable = PageRequest.of(0, size);

        boolean isFirstPage = (cursor == null || cursor.isBlank() || "-1".equals(cursor));
        Slice<Review> reviewSlice;

        if ("star".equalsIgnoreCase(sort)) {
            // 별점 순
            if (isFirstPage) {
                reviewSlice = reviewRepository.findFirstSliceByStarDesc(memberId, pageable);
            } else {
                // cursor 형식: "별점_ID" (ex) 4.5_16)
                String[] parts = cursor.split("_");
                if (parts.length != 2) {
                    throw new ReviewException(ReviewErrorCode.INVALID_CURSOR_FORMAT);
                }
                BigDecimal cursorStar;
                Long cursorId;
                try {
                    cursorStar = new BigDecimal(parts[0]);
                    cursorId = Long.parseLong(parts[1]);
                } catch (NumberFormatException e) {
                    throw new ReviewException(ReviewErrorCode.INVALID_CURSOR_FORMAT);
                }
                reviewSlice = reviewRepository.findNextSliceByStarDesc(memberId, cursorStar, cursorId, pageable);
            }
        } else {
            // ID 순 (기본)
            if (isFirstPage) {
                reviewSlice = reviewRepository.findFirstSliceByMemberId(memberId, pageable);
            } else {
                Long cursorId;
                try {
                    cursorId = Long.parseLong(cursor);
                } catch (NumberFormatException e) {
                    throw new ReviewException(ReviewErrorCode.INVALID_CURSOR_FORMAT);
                }
                reviewSlice = reviewRepository.findNextSliceByMemberId(memberId, cursorId, pageable);
            }
        }

        String nextCursor = calculateNextCursor(reviewSlice, sort);
        return ReviewConverter.toMyReviewSliceList(reviewSlice, nextCursor);
    }

    // 다음 커서 만들기 (마지막 아이템 기준)
    private String calculateNextCursor(Slice<Review> slice, String sort) {
        if (!slice.hasNext() || slice.getContent().isEmpty()) {
            return null; // 다음 페이지 없음
        }
        Review last = slice.getContent().get(slice.getContent().size() - 1);

        if (sort.equalsIgnoreCase("star")) {
            return last.getStar() + "_" + last.getId(); // 별점_아이디
        }
        return String.valueOf(last.getId()); // 아이디만
    }
}