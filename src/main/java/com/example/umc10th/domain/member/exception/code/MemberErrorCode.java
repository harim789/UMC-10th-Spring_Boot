package com.example.umc10th.domain.member.exception.code;

import com.example.umc10th.global.apiPayload.code.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum MemberErrorCode implements BaseErrorCode {
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "MEMBER404_1", "회원을 찾을 수 없습니다."),
    MEMBER_ALREADY_EXISTS(HttpStatus.CONFLICT, "MEMBER409_1", "이미 존재하는 유저입니다."),

    // 회원가입 관련 에러
    FOOD_NOT_FOUND(HttpStatus.BAD_REQUEST, "MEMBER400_1", "유효하지 않은 선호 음식입니다."),
    TERM_NOT_FOUND(HttpStatus.BAD_REQUEST, "MEMBER400_2", "유효하지 않은 약관입니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
