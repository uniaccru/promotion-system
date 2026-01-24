package com.grading.service.impl;

import com.grading.dto.request.CreateComparisonRequest;
import com.grading.dto.response.ComparisonResponse;
import com.grading.entity.*;
import com.grading.exception.BusinessLogicException;
import com.grading.exception.ResourceNotFoundException;
import com.grading.repository.*;
import com.grading.service.ComparisonService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ComparisonServiceImpl implements ComparisonService {
    private static final Logger logger = LoggerFactory.getLogger(ComparisonServiceImpl.class);
    private final ComparisonRepository comparisonRepository;
    private final CalibrationRepository calibrationRepository;
    private final EmployeeRepository employeeRepository;
    private final PromotionRequestRepository promotionRequestRepository;
    private final CalibrationEvaluatorRepository calibrationEvaluatorRepository;

    @Override
    @Transactional
    public ComparisonResponse createComparison(CreateComparisonRequest request, Long decidedById) {
        Calibration calibration = calibrationRepository.findById(request.getCalibrationId())
            .orElseThrow(() -> new RuntimeException("Calibration not found"));

        Employee candidateA = employeeRepository.findById(request.getCandidateAId())
            .orElseThrow(() -> new RuntimeException("Candidate A not found"));

        Employee candidateB = employeeRepository.findById(request.getCandidateBId())
            .orElseThrow(() -> new RuntimeException("Candidate B not found"));

        Employee winner = employeeRepository.findById(request.getWinnerId())
            .orElseThrow(() -> new RuntimeException("Winner not found"));

        Employee decidedBy = employeeRepository.findById(decidedById)
            .orElseThrow(() -> new RuntimeException("Decider not found"));

        if (!winner.getId().equals(candidateA.getId()) && !winner.getId().equals(candidateB.getId())) {
            throw new RuntimeException("Winner must be one of the candidates");
        }

        List<Comparison> existing = comparisonRepository.findByCalibrationIdWithRelations(request.getCalibrationId());
        boolean alreadyExists = existing.stream()
            .anyMatch(c -> {
                Employee cDecidedBy = c.getDecidedBy();
                if (cDecidedBy == null || !cDecidedBy.getId().equals(decidedById)) {
                    return false;
                }
                return (c.getCandidateA().getId().equals(candidateA.getId()) && c.getCandidateB().getId().equals(candidateB.getId())) ||
                       (c.getCandidateA().getId().equals(candidateB.getId()) && c.getCandidateB().getId().equals(candidateA.getId()));
            });

        if (alreadyExists) {
            throw new BusinessLogicException("You have already compared these candidates");
        }

        Comparison comparison = new Comparison();
        comparison.setCalibration(calibration);
        comparison.setCandidateA(candidateA);
        comparison.setCandidateB(candidateB);
        comparison.setDecidedBy(decidedBy);
        comparison.setWinner(winner);
        comparison.setDecidedAt(LocalDateTime.now());

        Comparison saved = comparisonRepository.save(comparison);
        return toComparisonResponse(saved, winner);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ComparisonResponse> getComparisonsByCalibrationId(Long calibrationId) {
        List<Comparison> comparisons = comparisonRepository.findByCalibrationIdWithRelations(calibrationId);
        return comparisons.stream()
            .map(c -> toComparisonResponse(c, c.getWinner()))
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ComparisonResponse> getPendingComparisonsForEvaluator(Long calibrationId, Long evaluatorId) {
        List<PromotionRequest> promotionRequests = promotionRequestRepository.findByCalibrationIdWithEmployee(calibrationId);
        
        if (promotionRequests.isEmpty()) {
            logger.debug("No promotion requests found for calibration {}", calibrationId);
            return new ArrayList<>();
        }

        List<Employee> candidates = promotionRequests.stream()
            .map(PromotionRequest::getEmployee)
            .filter(employee -> employee != null)
            .distinct()
            .collect(Collectors.toList());
        
        if (candidates.isEmpty()) {
            logger.debug("No candidates found for calibration {}", calibrationId);
            return new ArrayList<>();
        }

        List<Comparison> allComparisons = comparisonRepository.findByCalibrationIdWithRelations(calibrationId);
        
        List<Comparison> existingComparisons = allComparisons.stream()
            .filter(c -> {
                Employee decidedBy = c.getDecidedBy();
                return decidedBy != null && decidedBy.getId().equals(evaluatorId);
            })
            .collect(Collectors.toList());
        
        List<ComparisonResponse> pendingComparisons = new ArrayList<>();
        for (int i = 0; i < candidates.size(); i++) {
            for (int j = i + 1; j < candidates.size(); j++) {
                Employee candidateA = candidates.get(i);
                Employee candidateB = candidates.get(j);
                
                if (candidateA == null || candidateB == null) {
                    continue;
                }
                
                boolean exists = existingComparisons.stream()
                    .anyMatch(c -> {
                        Employee cA = c.getCandidateA();
                        Employee cB = c.getCandidateB();
                        if (cA == null || cB == null) {
                            return false;
                        }
                        return (cA.getId().equals(candidateA.getId()) && cB.getId().equals(candidateB.getId())) ||
                               (cA.getId().equals(candidateB.getId()) && cB.getId().equals(candidateA.getId()));
                    });

                if (!exists) {
                    ComparisonResponse response = new ComparisonResponse();
                    response.setCalibrationId(calibrationId);
                    response.setCandidateAId(candidateA.getId());
                    response.setCandidateAName(candidateA.getFullName() != null ? candidateA.getFullName() : "");
                    
                    PromotionRequest prA = promotionRequests.stream()
                        .filter(pr -> pr.getEmployee().getId().equals(candidateA.getId()))
                        .findFirst()
                        .orElse(null);
                    response.setCandidateAJustification(prA != null ? prA.getJustification() : "");
                    
                    response.setCandidateBId(candidateB.getId());
                    response.setCandidateBName(candidateB.getFullName() != null ? candidateB.getFullName() : "");
                    
                    PromotionRequest prB = promotionRequests.stream()
                        .filter(pr -> pr.getEmployee().getId().equals(candidateB.getId()))
                        .findFirst()
                        .orElse(null);
                    response.setCandidateBJustification(prB != null ? prB.getJustification() : "");
                    
                    pendingComparisons.add(response);
                }
            }
        }

        return pendingComparisons;
    }

    private ComparisonResponse toComparisonResponse(Comparison comparison, Employee winner) {
        ComparisonResponse response = new ComparisonResponse();
        response.setId(comparison.getId());
        response.setCalibrationId(comparison.getCalibration().getId());
        response.setCandidateAId(comparison.getCandidateA().getId());
        response.setCandidateAName(comparison.getCandidateA().getFullName());
        
        PromotionRequest prA = promotionRequestRepository.findByCalibrationIdAndEmployeeId(
            comparison.getCalibration().getId(), comparison.getCandidateA().getId());
        response.setCandidateAJustification(prA != null ? prA.getJustification() : "");
        
        response.setCandidateBId(comparison.getCandidateB().getId());
        response.setCandidateBName(comparison.getCandidateB().getFullName());
        
        PromotionRequest prB = promotionRequestRepository.findByCalibrationIdAndEmployeeId(
            comparison.getCalibration().getId(), comparison.getCandidateB().getId());
        response.setCandidateBJustification(prB != null ? prB.getJustification() : "");
        
        response.setWinnerId(winner.getId());
        response.setWinnerName(winner.getFullName());
        response.setDecidedById(comparison.getDecidedBy().getId());
        response.setDecidedByName(comparison.getDecidedBy().getFullName());
        response.setDecidedAt(comparison.getDecidedAt());
        return response;
    }
}

