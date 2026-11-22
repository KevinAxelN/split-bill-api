package com.kevin.splitbill.controller;

import com.kevin.splitbill.dto.SettlementResponse;
import com.kevin.splitbill.service.SettlementService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/groups/{groupId}/settlement")
public class SettlementController {

    private final SettlementService settlementService;

    public SettlementController(SettlementService settlementService) {
        this.settlementService = settlementService;
    }

    @GetMapping
    public ResponseEntity<SettlementResponse> getSettlement(
            @PathVariable Long groupId
    ) {
        return ResponseEntity.ok(
                settlementService.calculateSettlement(groupId)
        );
    }
}
