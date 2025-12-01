package com.grading.repository;

import com.grading.entity.ManagerEvaluation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ManagerEvaluationRepository extends JpaRepository<ManagerEvaluation, Long> {
    List<ManagerEvaluation> findByEmployeeId(Long employeeId);
    List<ManagerEvaluation> findByEvaluatorId(Long evaluatorId);
    List<ManagerEvaluation> findByReviewPeriod(String reviewPeriod);
    
    @Query("SELECT me FROM ManagerEvaluation me WHERE me.employee.id = :employeeId ORDER BY me.createdAt DESC LIMIT 1")
    Optional<ManagerEvaluation> findLatestByEmployeeId(@Param("employeeId") Long employeeId);
}