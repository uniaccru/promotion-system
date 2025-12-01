package com.grading.service.impl;

import com.grading.dto.request.CreateCalibrationPackageRequest;
import com.grading.dto.response.CalibrationResponse;
import com.grading.dto.response.CandidateRankingResponse;
import com.grading.entity.*;
import com.grading.repository.*;
import com.grading.service.CalibrationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CalibrationServiceImpl implements CalibrationService {
    private final CalibrationRepository calibrationRepository;
    private final GradeRepository gradeRepository;
    private final EmployeeRepository employeeRepository;
    private final PromotionRequestRepository promotionRequestRepository;
    private final CalibrationEvaluatorRepository calibrationEvaluatorRepository;
    private final ComparisonRepository comparisonRepository;

    @Override
    @Transactional
    public Calibration createCalibration(Long gradeId, Long createdById) {
        Grade grade = gradeRepository.findById(gradeId)
            .orElseThrow(() -> new RuntimeException("Grade not found"));
        Employee createdBy = employeeRepository.findById(createdById)
            .orElseThrow(() -> new RuntimeException("Employee not found"));

        Calibration calibration = new Calibration();
        calibration.setGrade(grade);
        calibration.setCreatedBy(createdBy);
        calibration.setStatus("planning");

        return calibrationRepository.save(calibration);
    }

    @Override
    @Transactional
    public Calibration updateCalibrationStatus(Long id, String status) {
        Calibration calibration = calibrationRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Calibration not found"));

        calibration.setStatus(status);
        return calibrationRepository.save(calibration);
    }

    @Override
    public Calibration getCalibrationById(Long id) {
        return calibrationRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Calibration not found"));
    }

    @Override
    public List<Calibration> getCalibrationsByStatus(String status) {
        return calibrationRepository.findByStatus(status);
    }

    @Override
    public List<Calibration> getCalibrationsByGradeId(Long gradeId) {
        return calibrationRepository.findByGradeId(gradeId);
    }

    @Override
    @Transactional
    public CalibrationResponse createCalibrationPackage(CreateCalibrationPackageRequest request, Long createdById) {
        Grade grade = gradeRepository.findById(request.getGradeId())
            .orElseThrow(() -> new RuntimeException("Grade not found"));

        Employee createdBy = employeeRepository.findById(createdById)
            .orElseThrow(() -> new RuntimeException("Creator not found"));

        if (request.getPromotionRequestIds().size() < 3) {
            throw new RuntimeException("At least 3 promotion requests are required");
        }

        List<PromotionRequest> promotionRequests = request.getPromotionRequestIds().stream()
            .map(id -> promotionRequestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Promotion request not found: " + id)))
            .collect(Collectors.toList());

        for (PromotionRequest pr : promotionRequests) {
            if (!pr.getRequestedGrade().getId().equals(request.getGradeId())) {
                throw new RuntimeException("All promotion requests must be for the same grade");
            }
            String status = pr.getStatus();
            if (!status.equals("ready_for_calibration") && 
                !status.equals("pending") && 
                !status.equals("under_review")) {
                throw new RuntimeException("Promotion request " + pr.getId() + " is not ready for calibration (status: " + status + ")");
            }
            if (pr.getCalibration() != null) {
                throw new RuntimeException("Promotion request " + pr.getId() + " is already in a calibration");
            }
        }

        if (request.getEvaluatorIds().size() != 2) {
            throw new RuntimeException("Exactly 2 evaluators are required");
        }

        List<Employee> evaluators = request.getEvaluatorIds().stream()
            .map(id -> employeeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Evaluator not found: " + id)))
            .collect(Collectors.toList());

        Calibration calibration = new Calibration();
        calibration.setGrade(grade);
        calibration.setCreatedBy(createdBy);
        calibration.setStatus("planning");
        calibration = calibrationRepository.save(calibration);

        for (PromotionRequest pr : promotionRequests) {
            pr.setCalibration(calibration);
            pr.setStatus("in_calibration");
            promotionRequestRepository.save(pr);
        }

        for (Employee evaluator : evaluators) {
            CalibrationEvaluator ce = new CalibrationEvaluator();
            ce.setCalibration(calibration);
            ce.setEvaluator(evaluator);
            calibrationEvaluatorRepository.save(ce);
        }

        Calibration savedCalibration = calibrationRepository.findById(calibration.getId())
            .orElseThrow(() -> new RuntimeException("Calibration not found after save"));
        
        return toCalibrationResponse(savedCalibration);
    }

    @Override
    public CalibrationResponse getCalibrationResponseById(Long id) {
        Calibration calibration = getCalibrationById(id);
        return toCalibrationResponse(calibration);
    }

    @Override
    public List<CalibrationResponse> getCalibrationResponsesByStatus(String status) {
        return getCalibrationsByStatus(status).stream()
            .map(this::toCalibrationResponse)
            .collect(Collectors.toList());
    }

    @Override
    public List<CalibrationResponse> getCalibrationsByEvaluatorId(Long evaluatorId) {
        List<CalibrationEvaluator> evaluatorCalibrations = calibrationEvaluatorRepository.findByEvaluatorId(evaluatorId);
        System.out.println("Found " + evaluatorCalibrations.size() + " calibrations for evaluator " + evaluatorId);
        return evaluatorCalibrations.stream()
            .map(ce -> {
                Calibration cal = ce.getCalibration();
                System.out.println("Processing calibration " + cal.getId() + " for evaluator " + evaluatorId);
                return toCalibrationResponse(cal);
            })
            .collect(Collectors.toList());
    }

    @Override
    public CandidateRankingResponse getCandidateRanking(Long calibrationId) {
        Calibration calibration = getCalibrationById(calibrationId);
        List<Comparison> comparisons = comparisonRepository.findByCalibrationIdWithRelations(calibrationId);
        
        List<PromotionRequest> promotionRequests = promotionRequestRepository.findByCalibrationIdWithEmployee(calibrationId);

        List<CandidateRankingResponse.CandidateScore> rankings = promotionRequests.stream()
            .map(pr -> {
                Long employeeId = pr.getEmployee().getId();
                String employeeName = pr.getEmployee().getFullName();
                
                int wins = (int) comparisons.stream()
                    .filter(c -> c.getWinner() != null && c.getWinner().getId().equals(employeeId))
                    .count();
                
                int totalComparisons = (int) comparisons.stream()
                    .filter(c -> (c.getCandidateA().getId().equals(employeeId) || c.getCandidateB().getId().equals(employeeId)))
                    .count();
                
                double winRate = totalComparisons > 0 ? (double) wins / totalComparisons : 0.0;
                
                CandidateRankingResponse.CandidateScore score = new CandidateRankingResponse.CandidateScore();
                score.setEmployeeId(employeeId);
                score.setEmployeeName(employeeName);
                score.setPromotionRequestId(pr.getId());
                score.setRequestedGradeName(pr.getRequestedGrade().getName());
                score.setCurrentStatus(pr.getStatus());
                score.setWins(wins);
                score.setTotalComparisons(totalComparisons);
                score.setWinRate(winRate);
                return score;
            })
            .sorted((a, b) -> Double.compare(b.getWinRate(), a.getWinRate()))
            .collect(Collectors.toList());

        CandidateRankingResponse response = new CandidateRankingResponse();
        response.setCalibrationId(calibrationId);
        response.setRankings(rankings);
        return response;
    }

    private CalibrationResponse toCalibrationResponse(Calibration calibration) {
        CalibrationResponse response = new CalibrationResponse();
        response.setId(calibration.getId());
        response.setGradeId(calibration.getGrade().getId());
        response.setGradeName(calibration.getGrade().getName());
        response.setCreatedById(calibration.getCreatedBy().getId());
        response.setCreatedByName(calibration.getCreatedBy().getFullName());
        response.setStatus(calibration.getStatus());
        response.setCreatedAt(calibration.getCreatedAt());
        response.setUpdatedAt(calibration.getUpdatedAt());

        List<CalibrationEvaluator> evaluators = calibrationEvaluatorRepository.findByCalibrationId(calibration.getId());
        response.setEvaluatorIds(evaluators.stream()
            .map(ce -> ce.getEvaluator().getId())
            .collect(Collectors.toList()));
        response.setEvaluatorNames(evaluators.stream()
            .map(ce -> ce.getEvaluator().getFullName())
            .collect(Collectors.toList()));

        List<PromotionRequest> promotionRequests = promotionRequestRepository.findByCalibrationId(calibration.getId());
        response.setPromotionRequestIds(promotionRequests.stream()
            .map(PromotionRequest::getId)
            .collect(Collectors.toList()));
        response.setCandidateCount(promotionRequests.size());

        return response;
    }
}