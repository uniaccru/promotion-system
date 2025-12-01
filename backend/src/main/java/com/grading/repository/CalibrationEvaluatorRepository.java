package com.grading.repository;

import com.grading.entity.CalibrationEvaluator;
import com.grading.entity.CalibrationEvaluatorId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CalibrationEvaluatorRepository extends JpaRepository<CalibrationEvaluator, CalibrationEvaluatorId> {
    List<CalibrationEvaluator> findByCalibrationId(Long calibrationId);
    List<CalibrationEvaluator> findByEvaluatorId(Long evaluatorId);
}



