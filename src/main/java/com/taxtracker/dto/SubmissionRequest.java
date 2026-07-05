package com.taxtracker.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SubmissionRequest {

    @NotNull(message = "Please provide a valid formId")
    private Long formId;
}
