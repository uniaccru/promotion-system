package com.grading.repository;

import com.grading.entity.PromotionRequestFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PromotionRequestFileRepository extends JpaRepository<PromotionRequestFile, Long> {
    @Query("SELECT prf FROM PromotionRequestFile prf WHERE prf.promotionRequest.id = :promotionRequestId ORDER BY prf.uploadedAt DESC")
    List<PromotionRequestFile> findByPromotionRequestId(@Param("promotionRequestId") Long promotionRequestId);
    
    void deleteByPromotionRequestId(Long promotionRequestId);
}
