package com.example.umc10th.domain.member.converter;

import com.example.umc10th.domain.member.dto.MemberReqDTO;
import com.example.umc10th.domain.member.dto.MemberResDTO;
import com.example.umc10th.domain.member.entity.Member;
import com.example.umc10th.domain.member.enums.Gender;
import com.example.umc10th.domain.mission.entity.mapping.MemberMission;
import com.example.umc10th.global.dto.PageInfoDTO;
import com.example.umc10th.global.enums.Address;
import org.springframework.data.domain.Page;

import java.util.List;

public class MemberConverter {

    // 회원가입: 요청 DTO -> Member 엔티티
    public static Member toMember(MemberReqDTO.SignUp request, String encodedPassword) {
        return Member.builder()
                .name(request.name())
                .nickname(request.nickname())
                .email(request.email())
                .password(encodedPassword)
                .phoneNumber(request.phoneNumber())
                .gender(Gender.valueOf(request.gender()))
                .birth(request.birth())
                .address(Address.valueOf(request.address()))
                .detailAddress(request.detailAddress())
                // socialType, profileUrl, point는 엔티티의 @Builder.Default로 자동 처리
                // (LOCAL, 기본 프로필 URL, 0)
                .build();
    }

    // 회원가입: 저장된 Member 엔티티 -> 회원가입 응답 DTO
    public static MemberResDTO.SignUp toSignupResponse(Member member) {
        return MemberResDTO.SignUp.builder()
                .memberId(member.getId())
                .nickname(member.getNickname())
                .email(member.getEmail())
                .build();
    }

    // 마이페이지 변환
    public static MemberResDTO.MyPage toMyPage(Member member) {
        return MemberResDTO.MyPage.builder()
                .memberId(member.getId())
                .nickname(member.getNickname())
                .email(member.getEmail())
                .phoneNumber(member.getPhoneNumber())
                .point(member.getPoint())
                .profileUrl(member.getProfileUrl())
                .build();
    }

    // 홈 - 회원 정보
    public static MemberResDTO.HomeMember toHomeMember(Member member) {
        return MemberResDTO.HomeMember.builder()
                .memberId(member.getId())
                .nickname(member.getNickname())
                .point(member.getPoint())
                .build();
    }

    // 홈 - 미션 요약
    public static MemberResDTO.MissionSummary toMissionSummary(Long received, Long completed, Long inProgress) {
        return MemberResDTO.MissionSummary.builder()
                .receivedCount(received.intValue())
                .completedCount(completed.intValue())
                .inProgressCount(inProgress.intValue())
                .build();
    }

    // 홈 - 받은 미션 단건
    public static MemberResDTO.ReceivedMission toReceivedMission(MemberMission mm) {
        return MemberResDTO.ReceivedMission.builder()
                .memberMissionId(mm.getId())
                .missionId(mm.getMission().getId())
                .storeId(mm.getMission().getStore().getId())
                .storeName(mm.getMission().getStore().getName())
                .condition(mm.getMission().getConditional())
                .rewardPoint(mm.getMission().getPoint())
                .status(mm.isComplete() ? "COMPLETED" : "IN_PROGRESS")
                .deadline(mm.getMission().getDeadline())
                .build();
    }

    // 홈 - 전체 응답
    public static MemberResDTO.Home toHome(
            Member member, Long received, Long completed, Long inProgress,
            Page<MemberMission> missionPage
    ) {
        // 1. Page에서 실제 데이터(List)를 꺼내서 DTO로 변환
        List<MemberResDTO.ReceivedMission> missionList = missionPage.getContent().stream() // Page에서 실제 데이터 리스트를 가져옴
                .map(MemberConverter::toReceivedMission)
                .toList();

        return MemberResDTO.Home.builder()
                .member(toHomeMember(member)) // 회원 정보 변환
                .missionSummary(toMissionSummary(received, completed, inProgress)) // 미션 요약 변환
                .receivedMissions(missionList) // 받은 미션 리스트 설정
                .pageInfo(PageInfoDTO.from(missionPage)) // 페이지 정보 변환
                .build();
    }
}
