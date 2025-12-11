package com.grading.model;

import com.grading.entity.PromotionRequest;
import com.grading.entity.GoalAssignment;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Entity
@Table(name = "promotion_request_goals", schema = "grading2")
@Data
@NoArgsConstructor
@AllArgsConstructor
@IdClass(PromotionRequestGoal.PromotionRequestGoalId.class)
public class PromotionRequestGoal {

    @Id
    @Column(name = "promotion_request_id")
    private Long promotionRequestId;

    @Id
    @Column(name = "goal_assignment_id")
    private Long goalAssignmentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "promotion_request_id", insertable = false, updatable = false)
    private PromotionRequest promotionRequest;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "goal_assignment_id", insertable = false, updatable = false)
    private GoalAssignment goalAssignment;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PromotionRequestGoalId implements Serializable {
        private Long promotionRequestId;
        private Long goalAssignmentId;
    }
}
