package com.example.umc10th.domain.mission.controller;

import com.example.umc10th.domain.mission.dto.MissionReqDTO;
import com.example.umc10th.domain.mission.dto.MissionResDTO;
import com.example.umc10th.domain.mission.exception.code.MissionSuccessCode;
import com.example.umc10th.domain.mission.service.MissionService;
import com.example.umc10th.global.apiPayload.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
@Validated
public class MissionController {

    private final MissionService missionService;

    // 4. 미션 목록 조회 (진행중 / 진행완료 통합)
    @GetMapping("/member-missions")
    public ApiResponse<MissionResDTO.MissionList> getMemberMissions(
            @RequestParam Long memberId,
            @RequestParam
            @Pattern(regexp = "^(IN_PROGRESS|COMPLETED)$",
                    message = "status는 IN_PROGRESS 또는 COMPLETED만 가능합니다.")
            String status, // "IN_PROGRESS" or "COMPLETED"
            @RequestParam(defaultValue = "0")
            @Min(value = 0, message = "page는 0 이상이어야 합니다.")
            Integer page,
            @RequestParam(defaultValue = "10")
            @Min(value = 1, message = "size는 1 이상이어야 합니다.")
            @Max(value = 100, message = "size는 100 이하여야 합니다.")
            Integer size
    ) {
        MissionResDTO.MissionList result = missionService.getMemberMissions(memberId, status, page, size);
        return ApiResponse.onSuccess(MissionSuccessCode.GET_MISSION_LIST, result);
    }

    // 4(1). 내가 진행중인 미션만 조회 (사용자 ID는 쿼리파라미터로 받음, 오프셋 기반 페이지네이션)
    @GetMapping("/member-missions/in-progress")
    public ApiResponse<MissionResDTO.MissionList> getMyInProgressMissions(
            @RequestParam Long memberId,
            @RequestParam(defaultValue = "0")
            @Min(value = 0, message = "page는 0 이상이어야 합니다.")
            Integer page,
            @RequestParam(defaultValue = "10")
            @Min(value = 1, message = "size는 1 이상이어야 합니다.")
            @Max(value = 100, message = "size는 100 이하여야 합니다.") //클라이언트가 비정상적으로 큰 값을 보내는 것을 방지하기 위해 최대값 설정
            Integer size
    ) {
        MissionResDTO.MissionList result = missionService.getMyInProgressMissions(memberId, page, size);
        return ApiResponse.onSuccess(MissionSuccessCode.GET_MISSION_LIST, result);
    }


    // 5. 미션 성공 누르기
    @PatchMapping("/member-missions/{memberMissionId}/complete")
    public ApiResponse<MissionResDTO.CompleteMission> completeMission(
            @PathVariable Long memberMissionId,
            @Valid @RequestBody MissionReqDTO.CompleteMission request
    ) {
        // 6주차에서 missionService.completeMission(memberMissionId, request)로 교체
        MissionResDTO.CompleteMission result = MissionResDTO.CompleteMission.builder()
                .memberMissionId(memberMissionId)
                .status("COMPLETED")
                .earnedPoint(500)
                .currentPoint(3000)
                .completedAt(LocalDateTime.now())
                .build();

        return ApiResponse.onSuccess(MissionSuccessCode.COMPLETE_MISSION, result);
    }
}
