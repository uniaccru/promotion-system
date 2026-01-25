package com.grading.repository;

import com.grading.entity.Calibration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CalibrationRepository extends JpaRepository<Calibration, Long> {
    @Query("SELECT c FROM Calibration c WHERE c.status = :status ORDER BY c.createdAt DESC")
    List<Calibration> findByStatus(@Param("status") String status);
    
    @Query("SELECT c FROM Calibration c WHERE c.grade.id = :gradeId ORDER BY c.createdAt DESC")
    List<Calibration> findByGradeId(@Param("gradeId") Long gradeId);
    
    @Query("SELECT c FROM Calibration c ORDER BY c.createdAt DESC")
    @Override
    List<Calibration> findAll();
}