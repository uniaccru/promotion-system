package com.grading.repository;

import com.grading.entity.GoalAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GoalAssignmentRepository extends JpaRepository<GoalAssignment, Long> {
    List<GoalAssignment> findByEmployeeId(Long employeeId);
    
    @Query("SELECT ga FROM GoalAssignment ga WHERE ga.employee.id = :employeeId AND ga.goal.reviewPeriod = :reviewPeriod")
    List<GoalAssignment> findByEmployeeIdAndReviewPeriod(@Param("employeeId") Long employeeId, @Param("reviewPeriod") String reviewPeriod);
    
    @Query("SELECT ga FROM GoalAssignment ga " +
           "LEFT JOIN FETCH ga.goal " +
           "LEFT JOIN FETCH ga.employee " +
           "LEFT JOIN FETCH ga.employee.user")
    List<GoalAssignment> findAllWithRelations();
}