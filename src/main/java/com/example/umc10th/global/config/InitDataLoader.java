package com.example.umc10th.global.config;

import com.example.umc10th.domain.member.entity.Food;
import com.example.umc10th.domain.member.entity.Term;
import com.example.umc10th.domain.member.repository.FoodRepository;
import com.example.umc10th.domain.member.repository.TermRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;

@Slf4j
@Component
@RequiredArgsConstructor
// CommandLineRunner:  Spring이 제공하는 인터페이스
// 이걸 구현하면 애플리케이션이 시작된 직후에 run() 메서드가 자동으로 호출된다.
public class InitDataLoader implements CommandLineRunner {

    private final FoodRepository foodRepository;
    private final TermRepository termRepository;

    @Override
    @Transactional
    public void run(String... args) {
        seedFoods();
        seedTerms();
    }

    private void seedFoods() {
        // enum의 모든 값을 순회하면서, DB에 없는 것만 INSERT
        Arrays.stream(com.example.umc10th.domain.member.enums.Food.values())
                .filter(foodEnum -> foodRepository.findByName(foodEnum).isEmpty())
                .forEach(foodEnum -> {
                    foodRepository.save(Food.builder()
                            .name(foodEnum)
                            .build());
                    log.info("[InitDataLoader] Food 시드: {}", foodEnum);
                });
    }

    private void seedTerms() {
        Arrays.stream(com.example.umc10th.domain.member.enums.Term.values())
                .filter(termEnum -> termRepository.findByName(termEnum).isEmpty())
                .forEach(termEnum -> {
                    termRepository.save(Term.builder()
                            .name(termEnum)
                            .build());
                    log.info("[InitDataLoader] Term 시드: {}", termEnum);
                });
    }
}
