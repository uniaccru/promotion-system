package com.grading.dto.response;

import lombok.Data;

import java.util.List;

@Data
public class CandidateRankingResponse {
    private Long calibrationId;
    private List<CandidateScore> rankings;

    @Data
    public static class CandidateScore {
        private Long employeeId;
        private String employeeName;
        private Long promotionRequestId;
        private String requestedGradeName;
        private String currentStatus;
        private Integer wins;
        private Integer totalComparisons;
        private Double winRate;
    }
}



