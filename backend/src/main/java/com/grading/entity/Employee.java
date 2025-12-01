package com.grading.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "employees", schema = "grading2")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"user", "gradeHistories", "goalAssignments", "receivedEvaluations", "givenEvaluations", "promotionRequests"})
public class Employee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String role;

    @Column(name = "hire_date", nullable = false)
    private LocalDate hireDate;

    @Column(nullable = false)
    private String department;

    @Column(name = "review_period", nullable = false)
    private String reviewPeriod;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "employee")
    private List<GradeHistory> gradeHistories;

    @OneToMany(mappedBy = "employee")
    private List<GoalAssignment> goalAssignments;

    @OneToMany(mappedBy = "employee")
    private List<ManagerEvaluation> receivedEvaluations;

    @OneToMany(mappedBy = "evaluator")
    private List<ManagerEvaluation> givenEvaluations;

    @OneToMany(mappedBy = "employee")
    private List<PromotionRequest> promotionRequests;
}