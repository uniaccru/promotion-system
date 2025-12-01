package com.grading.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "grade_history", schema = "grading2")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GradeHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @ManyToOne
    @JoinColumn(name = "old_grade_id", nullable = false)
    private Grade oldGrade;

    @ManyToOne
    @JoinColumn(name = "new_grade_id", nullable = false)
    private Grade newGrade;

    @ManyToOne
    @JoinColumn(name = "changed_by", nullable = false)
    private Employee changedBy;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String reason;

    @CreationTimestamp
    @Column(name = "changed_at", nullable = false, updatable = false)
    private LocalDateTime changedAt;
}