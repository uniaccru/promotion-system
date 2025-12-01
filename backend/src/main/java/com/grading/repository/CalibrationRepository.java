package com.grading.repository;

import com.grading.entity.Calibration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CalibrationRepository extends JpaRepository<Calibration, Long> {
    List<Calibration> findByStatus(String status);
    List<Calibration> findByGradeId(Long gradeId);
}