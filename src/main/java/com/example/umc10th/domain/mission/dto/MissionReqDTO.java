package com.example.umc10th.domain.mission.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;

public class MissionReqDTO {

    // 미션 성공 누르기
    @Builder
    public record CompleteMission(
            @NotNull(message = "완료 여부는 필수입니다.")
            Boolean completed
    ) {}
}
