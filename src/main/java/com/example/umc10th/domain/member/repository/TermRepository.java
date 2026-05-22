package com.example.umc10th.domain.member.repository;

import com.example.umc10th.domain.member.entity.Term;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TermRepository extends JpaRepository<Term, Long> {
    Optional<Term> findByName(com.example.umc10th.domain.member.enums.Term name);
}
