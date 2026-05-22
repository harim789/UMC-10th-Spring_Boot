package com.example.umc10th.domain.member.controller;

import com.example.umc10th.domain.member.dto.MemberReqDTO;
import com.example.umc10th.domain.member.dto.MemberResDTO;
import com.example.umc10th.domain.member.exception.code.MemberSuccessCode;
import com.example.umc10th.domain.member.service.MemberService;
import com.example.umc10th.global.apiPayload.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
@Validated
public class MemberController {

    private final MemberService memberService;

    // 1. 회원가입
    @PostMapping("/auth/signup")
    public ApiResponse<MemberResDTO.SignUp> signUp(
            @Valid @RequestBody MemberReqDTO.SignUp request
    ) {
        MemberResDTO.SignUp result = memberService.signUp(request);
        return ApiResponse.onSuccess(MemberSuccessCode.SIGN_UP, result);
    }

    // 2. 홈 화면 조회
    @GetMapping("/home")
    public ApiResponse<MemberResDTO.Home> getHome(
            @RequestParam Long memberId,
            @RequestParam(defaultValue = "0")
            @Min(value = 0, message = "page는 0 이상이어야 합니다.")
            Integer page,
            @RequestParam(defaultValue = "10")
            @Min(value = 1, message = "size는 1 이상이어야 합니다.")
            @Max(value = 100, message = "size는 100 이하여야 합니다.")
            Integer size
    ) { // @RequestParam: URL 뒤에 붙는 쿼리 파라미터를 받을 때 쓴다.
        MemberResDTO.Home result = memberService.getHome(memberId, page, size);
        return ApiResponse.onSuccess(MemberSuccessCode.GET_HOME, result);
    }

    // 3. 마이페이지 조회
    @GetMapping("/members/me")
    public ApiResponse<MemberResDTO.MyPage> getMyPage(@RequestParam Long memberId) {
        MemberResDTO.MyPage result = memberService.getMyPage(memberId);
        return ApiResponse.onSuccess(MemberSuccessCode.GET_MY_PAGE, result);
    }
}
