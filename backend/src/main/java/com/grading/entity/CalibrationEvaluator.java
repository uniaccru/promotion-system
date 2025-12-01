package com.grading.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "calibration_evaluators", schema = "grading2")
@Data
@NoArgsConstructor
@AllArgsConstructor
@IdClass(CalibrationEvaluatorId.class)
public class CalibrationEvaluator {
    @Id
    @ManyToOne
    @JoinColumn(name = "calibration_id", nullable = false)
    @JsonIgnore
    private Calibration calibration;

    @Id
    @ManyToOne
    @JoinColumn(name = "evaluator_id", nullable = false)
    private Employee evaluator;
}

