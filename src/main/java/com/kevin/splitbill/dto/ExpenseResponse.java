package com.kevin.splitbill.dto;

import java.math.BigDecimal;
import java.util.List;

public record ExpenseResponse(
        Long id,
        String description,
        BigDecimal amount,
        Long paidByParticipantId,
        String paidByParticipantName,
        List<ExpenseSplitResponse> splits
) {}