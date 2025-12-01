package com.grading.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "calibrations", schema = "grading2")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Calibration {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "grade_id", nullable = false)
    private Grade grade;

    @ManyToOne
    @JoinColumn(name = "created_by", nullable = false)
    private Employee createdBy;

    @Column(nullable = false)
    private String status;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "calibration")
    @JsonIgnore
    private List<Comparison> comparisons;

    @OneToMany(mappedBy = "calibration", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<CalibrationEvaluator> evaluators;

    @OneToMany(mappedBy = "calibration")
    @JsonIgnore
    private List<PromotionRequest> promotionRequests;
}