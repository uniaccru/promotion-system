package com.grading.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name = "grades", schema = "grading2")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Grade {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String name;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @OneToMany(mappedBy = "oldGrade")
    private List<GradeHistory> oldGradeHistories;

    @OneToMany(mappedBy = "newGrade")
    private List<GradeHistory> newGradeHistories;

    @OneToMany(mappedBy = "requestedGrade")
    private List<PromotionRequest> promotionRequests;

    @OneToMany(mappedBy = "grade")
    private List<Calibration> calibrations;
}