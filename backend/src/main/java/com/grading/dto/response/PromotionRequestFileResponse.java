package com.grading.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PromotionRequestFileResponse {
    private Long id;
    private Long promotionRequestId;
    private String fileName;
    private Long fileSize;
    private String contentType;
    private LocalDateTime uploadedAt;
}
