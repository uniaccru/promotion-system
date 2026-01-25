package com.grading.repository;

import com.grading.entity.Goal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GoalRepository extends JpaRepository<Goal, Long> {
    @Query("SELECT g FROM Goal g WHERE g.reviewPeriod = :reviewPeriod ORDER BY g.createdAt DESC")
    List<Goal> findByReviewPeriod(@Param("reviewPeriod") String reviewPeriod);
    
    @Query("SELECT g FROM Goal g ORDER BY g.createdAt DESC")
    @Override
    List<Goal> findAll();
}

