package com.grading.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "promotion_requests", schema = "grading2")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PromotionRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @ManyToOne
    @JoinColumn(name = "requested_grade_id", nullable = false)
    private Grade requestedGrade;

    @ManyToOne
    @JoinColumn(name = "submitted_by_id", nullable = false)
    private Employee submittedBy;

    @ManyToOne
    @JoinColumn(name = "status_changed_by_id", nullable = false)
    private Employee statusChangedBy;

    @ManyToOne
    @JoinColumn(name = "calibration_id", nullable = true)
    @JsonIgnore
    private Calibration calibration;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String justification;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String evidence;

    @Column(name = "review_period", nullable = false)
    private String reviewPeriod;

    @Column(nullable = false)
    private String status;

    @Column(name = "hr_comment", columnDefinition = "TEXT")
    private String hrComment;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "promotionRequest")
    private List<PromotionRequestReviewer> reviewers;
}