package com.grading.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "manager_evaluations", schema = "grading2")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ManagerEvaluation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "evaluator_id", nullable = false)
    private Employee evaluator;

    @ManyToOne
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Column(name = "review_period", nullable = false)
    private String reviewPeriod;

    @Column(nullable = false)
    private BigDecimal score;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String comment;

    @Column(name = "nominated_for_promotion", nullable = false)
    private Boolean nominatedForPromotion;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}