package com.kevin.splitbill.dto;

import java.util.List;

public record BillGroupResponse(
        Long id,
        String name,
        List<ParticipantResponse> participants
) {
}