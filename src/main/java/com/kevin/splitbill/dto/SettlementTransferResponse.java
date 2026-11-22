package com.kevin.splitbill.dto;

import java.math.BigDecimal;

public record SettlementTransferResponse(
        Long fromParticipantId,
        String fromParticipantName,
        Long toParticipantId,
        String toParticipantName,
        BigDecimal amount
) {}