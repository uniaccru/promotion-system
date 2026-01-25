package com.grading.repository;

import com.grading.entity.PromotionRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PromotionRequestRepository extends JpaRepository<PromotionRequest, Long> {
    @Query("SELECT pr FROM PromotionRequest pr WHERE pr.employee.id = :employeeId ORDER BY pr.createdAt DESC")
    List<PromotionRequest> findByEmployeeId(@Param("employeeId") Long employeeId);
    
    @Query("SELECT pr FROM PromotionRequest pr WHERE pr.status = :status ORDER BY pr.createdAt DESC")
    List<PromotionRequest> findByStatus(@Param("status") String status);
    
    @Query("SELECT pr FROM PromotionRequest pr WHERE pr.reviewPeriod = :reviewPeriod ORDER BY pr.createdAt DESC")
    List<PromotionRequest> findByReviewPeriod(@Param("reviewPeriod") String reviewPeriod);
    
    @Query("SELECT COUNT(pr) > 0 FROM PromotionRequest pr WHERE pr.employee.id = :employeeId " +
           "AND pr.requestedGrade.id = :gradeId AND pr.status IN :statuses")
    boolean existsActiveRequest(@Param("employeeId") Long employeeId, 
                                @Param("gradeId") Long gradeId, 
                                @Param("statuses") List<String> statuses);
    
    @Query("SELECT pr FROM PromotionRequest pr WHERE pr.calibration.id = :calibrationId ORDER BY pr.createdAt DESC")
    List<PromotionRequest> findByCalibrationId(@Param("calibrationId") Long calibrationId);
    
    @Query("SELECT pr FROM PromotionRequest pr " +
           "LEFT JOIN FETCH pr.employee " +
           "WHERE pr.calibration.id = :calibrationId ORDER BY pr.createdAt DESC")
    List<PromotionRequest> findByCalibrationIdWithEmployee(@Param("calibrationId") Long calibrationId);
    
    @Query("SELECT pr FROM PromotionRequest pr " +
           "WHERE pr.calibration.id = :calibrationId AND pr.employee.id = :employeeId")
    PromotionRequest findByCalibrationIdAndEmployeeId(@Param("calibrationId") Long calibrationId, 
                                                       @Param("employeeId") Long employeeId);
    
    @Query("SELECT pr FROM PromotionRequest pr ORDER BY pr.createdAt DESC")
    @Override
    List<PromotionRequest> findAll();
}