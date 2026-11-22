package com.kevin.splitbill.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotEmpty;

import java.math.BigDecimal;
import java.util.List;

public record CreateExpenseRequest(
        @NotBlank(message = "Description is required")
        String description,

        @NotNull
        @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
        BigDecimal amount,

        @NotNull(message = "Paid by participant ID is required")
        Long paidByParticipantId,

        @NotEmpty(message = "Split participants cannot be empty")
        List<@NotNull Long> splitParticipantIds
) {}