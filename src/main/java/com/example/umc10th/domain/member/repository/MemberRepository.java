package com.example.umc10th.domain.member.repository;

import com.example.umc10th.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

// JpaRepository: Spring Data JPA가 제공하는 인터페이스로, 기본적인 CRUD 메서드를 자동으로 구현해준다.
// <Member, Long>: 이 Repository는 Member 엔터티를 관리하며, Member 엔터티의 기본 키(PK) 타입이 Long임을 나타낸다.
public interface MemberRepository extends JpaRepository<Member, Long> {

    // 회원가입 시 이메일 중복 체크용
    boolean existsByEmail(String email);

    // 로그인에서 사용자 이메일로 회원 조회
    Optional<Member> findByEmail(String email);
}
