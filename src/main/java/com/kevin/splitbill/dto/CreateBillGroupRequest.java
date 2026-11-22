package com.kevin.splitbill.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record CreateBillGroupRequest(

        @NotBlank(message = "Group name is required")
        String name,

        @NotEmpty(message = "Participants cannot be empty")
        List<@NotBlank(message = "Participant name cannot be blank") String> participants

) {}