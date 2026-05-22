package com.example.umc10th.domain.member.service;

import com.example.umc10th.domain.member.converter.MemberConverter;
import com.example.umc10th.domain.member.dto.MemberReqDTO;
import com.example.umc10th.domain.member.dto.MemberResDTO;
import com.example.umc10th.domain.member.entity.Food;
import com.example.umc10th.domain.member.entity.Member;
import com.example.umc10th.domain.member.entity.Term;
import com.example.umc10th.domain.member.entity.mapping.MemberFood;
import com.example.umc10th.domain.member.entity.mapping.MemberTerm;
import com.example.umc10th.domain.member.exception.MemberException;
import com.example.umc10th.domain.member.exception.code.MemberErrorCode;
import com.example.umc10th.domain.member.repository.FoodRepository;
import com.example.umc10th.domain.member.repository.MemberRepository;
import com.example.umc10th.domain.member.repository.TermRepository;
import com.example.umc10th.domain.mission.entity.mapping.MemberMission;
import com.example.umc10th.domain.mission.repository.MemberMissionRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor // final 필드를 파라미터로 받는 생성자를 자동으로 생성해준다. (의존성 주입 편리하게)
@Transactional(readOnly = true)
public class MemberService {

    private final MemberRepository memberRepository;
    private final MemberMissionRepository memberMissionRepository;
    private final FoodRepository foodRepository;
    private final TermRepository termRepository;
    private final PasswordEncoder passwordEncoder;

    @PersistenceContext
    private EntityManager em; // MemberFood/MemberTerm을 직접 persist하기 위해

    // 회원가입
    @Transactional
    public MemberResDTO.SignUp signUp(MemberReqDTO.SignUp request) {

        // 1. 이메일 중복 체크
        if (memberRepository.existsByEmail(request.email())) {
            throw new MemberException(MemberErrorCode.MEMBER_ALREADY_EXISTS);
        }

        // 2. 비밀번호 BCrypt 인코딩
        String encodedPassword = passwordEncoder.encode(request.password());

        // 3. Member 엔티티 생성 및 저장
        Member member = MemberConverter.toMember(request, encodedPassword);
        Member savedMember = memberRepository.save(member);

        // 4. 선호 음식 매핑 저장
        for (String foodName: request.preferredFoods()) {
            com.example.umc10th.domain.member.enums.Food foodEnum;
            try {
                foodEnum = com.example.umc10th.domain.member.enums.Food.valueOf(foodName);
            } catch (IllegalArgumentException e) {
                throw new MemberException(MemberErrorCode.FOOD_NOT_FOUND);
            }

            Food food = foodRepository.findByName(foodEnum)
                    .orElseThrow(() -> new MemberException(MemberErrorCode.FOOD_NOT_FOUND));

            MemberFood memberFood = MemberFood.builder()
                    .member(savedMember)
                    .food(food)
                    .build();

            em.persist(memberFood);
        }

        // 5. 약관 동의 매핑 저장
        for (String termName: request.agreedTerms()) {
            com.example.umc10th.domain.member.enums.Term termEnum;
            try {
                termEnum = com.example.umc10th.domain.member.enums.Term.valueOf(termName);
            } catch (IllegalArgumentException e) {
                throw new MemberException(MemberErrorCode.TERM_NOT_FOUND);
            }

            Term term = termRepository.findByName(termEnum)
                    .orElseThrow(() -> new MemberException(MemberErrorCode.TERM_NOT_FOUND));

            MemberTerm memberTerm = MemberTerm.builder()
                    .member(savedMember)
                    .term(term)
                    .build();

            em.persist(memberTerm);
        }

        return MemberConverter.toSignupResponse(savedMember);
    }

    // 마이페이지
    public MemberResDTO.MyPage getMyPage(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberException(MemberErrorCode.MEMBER_NOT_FOUND));
        return MemberConverter.toMyPage(member);
    }

    // 홈 화면
    public MemberResDTO.Home getHome(Long memberId, Integer page, Integer size) {
        // 1. Member 조회
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberException(MemberErrorCode.MEMBER_NOT_FOUND));

        // 2. 미션 카운트 조회
        Long received = memberMissionRepository.countByMemberId(memberId);
        Long completed = memberMissionRepository.countByMemberIdAndStatus(memberId, true);
        Long inProgress = memberMissionRepository.countByMemberIdAndStatus(memberId, false);

        // 3. Pageable 생성 후 페이징 쿼리 호출
        Pageable pageable = PageRequest.of(page, size);
        Page<MemberMission> missionPage = memberMissionRepository.findAllByMemberId(memberId, pageable);

        // 4. 컨버터로 변환
        return MemberConverter.toHome(member, received, completed, inProgress, missionPage);
    }
}
