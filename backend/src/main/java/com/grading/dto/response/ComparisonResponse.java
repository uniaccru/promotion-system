package com.grading.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ComparisonResponse {
    private Long id;
    private Long calibrationId;
    private Long candidateAId;
    private String candidateAName;
    private String candidateAJustification;
    private Long candidateBId;
    private String candidateBName;
    private String candidateBJustification;
    private Long winnerId;
    private String winnerName;
    private Long decidedById;
    private String decidedByName;
    private LocalDateTime decidedAt;
}



