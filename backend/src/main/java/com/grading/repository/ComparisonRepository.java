package com.grading.repository;

import com.grading.entity.Comparison;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ComparisonRepository extends JpaRepository<Comparison, Long> {
    @Query("SELECT c FROM Comparison c WHERE c.calibration.id = :calibrationId ORDER BY c.decidedAt DESC")
    List<Comparison> findByCalibrationId(@Param("calibrationId") Long calibrationId);
    
    @Query("SELECT c FROM Comparison c " +
           "LEFT JOIN FETCH c.decidedBy " +
           "LEFT JOIN FETCH c.candidateA " +
           "LEFT JOIN FETCH c.candidateB " +
           "LEFT JOIN FETCH c.winner " +
           "WHERE c.calibration.id = :calibrationId ORDER BY c.decidedAt DESC")
    List<Comparison> findByCalibrationIdWithRelations(@Param("calibrationId") Long calibrationId);
}