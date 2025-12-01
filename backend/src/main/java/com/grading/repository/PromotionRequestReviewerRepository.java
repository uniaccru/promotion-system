package com.grading.repository;

import com.grading.entity.PromotionRequestReviewer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PromotionRequestReviewerRepository extends JpaRepository<PromotionRequestReviewer, PromotionRequestReviewer.PromotionRequestReviewerId> {
    List<PromotionRequestReviewer> findByPromotionRequestId(Long promotionRequestId);
    List<PromotionRequestReviewer> findByReviewerId(Long reviewerId);
}