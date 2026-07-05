package com.taxtracker.dto;

import lombok.Data;

@Data
public class UploadResponse {
    private Long documentId;
    private String fileName;
    private String message;

    public UploadResponse() {
    }

    public UploadResponse(Long documentId, String fileName, String message) {
        this.documentId = documentId;
        this.fileName = fileName;
        this.message = message;
    }
}
