package com.grading.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "comparisons", schema = "grading2")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Comparison {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "calibration_id", nullable = false)
    @JsonIgnore
    private Calibration calibration;

    @ManyToOne
    @JoinColumn(name = "candidate_a_id", nullable = false)
    private Employee candidateA;

    @ManyToOne
    @JoinColumn(name = "candidate_b_id", nullable = false)
    private Employee candidateB;

    @ManyToOne
    @JoinColumn(name = "decided_by", nullable = false)
    private Employee decidedBy;

    @ManyToOne
    @JoinColumn(name = "winner_id", nullable = false)
    private Employee winner;

    @Column(name = "decided_at", nullable = false)
    private LocalDateTime decidedAt;
}