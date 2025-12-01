package com.grading.service;

import com.grading.dto.request.CreateCalibrationPackageRequest;
import com.grading.dto.response.CalibrationResponse;
import com.grading.dto.response.CandidateRankingResponse;
import com.grading.entity.Calibration;

import java.util.List;

public interface CalibrationService {
    Calibration createCalibration(Long gradeId, Long createdById);
    CalibrationResponse createCalibrationPackage(CreateCalibrationPackageRequest request, Long createdById);
    Calibration updateCalibrationStatus(Long id, String status);
    Calibration getCalibrationById(Long id);
    CalibrationResponse getCalibrationResponseById(Long id);
    List<Calibration> getCalibrationsByStatus(String status);
    List<CalibrationResponse> getCalibrationResponsesByStatus(String status);
    List<Calibration> getCalibrationsByGradeId(Long gradeId);
    List<CalibrationResponse> getCalibrationsByEvaluatorId(Long evaluatorId);
    CandidateRankingResponse getCandidateRanking(Long calibrationId);
}