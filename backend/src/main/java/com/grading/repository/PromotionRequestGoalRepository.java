package com.grading.repository;

import com.grading.model.PromotionRequestGoal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PromotionRequestGoalRepository extends JpaRepository<PromotionRequestGoal, PromotionRequestGoal.PromotionRequestGoalId> {

    @Query("SELECT prg FROM PromotionRequestGoal prg WHERE prg.promotionRequestId = :promotionRequestId")
    List<PromotionRequestGoal> findByPromotionRequestId(@Param("promotionRequestId") Long promotionRequestId);

    @Query("SELECT prg FROM PromotionRequestGoal prg JOIN FETCH prg.goalAssignment ga JOIN FETCH ga.goal WHERE prg.promotionRequestId = :promotionRequestId")
    List<PromotionRequestGoal> findByPromotionRequestIdWithGoals(@Param("promotionRequestId") Long promotionRequestId);

    void deleteByPromotionRequestId(Long promotionRequestId);
}
