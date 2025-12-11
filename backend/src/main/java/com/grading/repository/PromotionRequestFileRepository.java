package com.grading.repository;

import com.grading.entity.PromotionRequestFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PromotionRequestFileRepository extends JpaRepository<PromotionRequestFile, Long> {
    List<PromotionRequestFile> findByPromotionRequestId(Long promotionRequestId);
    void deleteByPromotionRequestId(Long promotionRequestId);
}
