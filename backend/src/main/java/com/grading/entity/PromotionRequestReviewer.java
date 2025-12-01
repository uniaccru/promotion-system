package com.grading.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "promotion_request_reviewers", schema = "grading2")
@Data
@NoArgsConstructor
@AllArgsConstructor
@IdClass(PromotionRequestReviewer.PromotionRequestReviewerId.class)
public class PromotionRequestReviewer {
    @Id
    @ManyToOne
    @JoinColumn(name = "promotion_request_id", nullable = false)
    private PromotionRequest promotionRequest;

    @Id
    @ManyToOne
    @JoinColumn(name = "reviewer_id", nullable = false)
    private Employee reviewer;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String decision;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String comment;

    @Column(name = "decided_at", nullable = false)
    private LocalDateTime decidedAt;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PromotionRequestReviewerId implements Serializable {
        private Long promotionRequest;
        private Long reviewer;
    }
}