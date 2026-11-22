package com.kevin.splitbill.dto;

import java.math.BigDecimal;

public record ExpenseSplitResponse(
        Long participantId,
        String participantName,
        BigDecimal amount
) {}