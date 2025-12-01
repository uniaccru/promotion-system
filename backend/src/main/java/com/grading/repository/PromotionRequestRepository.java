package com.grading.repository;

import com.grading.entity.PromotionRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PromotionRequestRepository extends JpaRepository<PromotionRequest, Long> {
    List<PromotionRequest> findByEmployeeId(Long employeeId);
    List<PromotionRequest> findByStatus(String status);
    List<PromotionRequest> findByReviewPeriod(String reviewPeriod);
    
    @Query("SELECT COUNT(pr) > 0 FROM PromotionRequest pr WHERE pr.employee.id = :employeeId " +
           "AND pr.requestedGrade.id = :gradeId AND pr.status IN :statuses")
    boolean existsActiveRequest(@Param("employeeId") Long employeeId, 
                                @Param("gradeId") Long gradeId, 
                                @Param("statuses") List<String> statuses);
    
    List<PromotionRequest> findByCalibrationId(Long calibrationId);
    
    @Query("SELECT pr FROM PromotionRequest pr " +
           "LEFT JOIN FETCH pr.employee " +
           "WHERE pr.calibration.id = :calibrationId")
    List<PromotionRequest> findByCalibrationIdWithEmployee(@Param("calibrationId") Long calibrationId);
    
    @Query("SELECT pr FROM PromotionRequest pr " +
           "WHERE pr.calibration.id = :calibrationId AND pr.employee.id = :employeeId")
    PromotionRequest findByCalibrationIdAndEmployeeId(@Param("calibrationId") Long calibrationId, 
                                                       @Param("employeeId") Long employeeId);
}