package com.kevin.splitbill.controller;

import com.kevin.splitbill.dto.BillGroupResponse;
import com.kevin.splitbill.dto.CreateBillGroupRequest;
import com.kevin.splitbill.service.BillGroupService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/groups")
public class BillGroupController {

    private final BillGroupService billGroupService;

    public BillGroupController(BillGroupService billGroupService) {
        this.billGroupService = billGroupService;
    }

    @PostMapping
    public ResponseEntity<BillGroupResponse> createGroup(
            @Valid @RequestBody CreateBillGroupRequest request
    ) {
        BillGroupResponse response = billGroupService.createGroup(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping
    public ResponseEntity<List<BillGroupResponse>> getAllGroups() {
        return ResponseEntity.ok(billGroupService.getAllGroups());
    }

    @GetMapping("/{id}")
    public ResponseEntity<BillGroupResponse> getGroupById(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(billGroupService.getGroupById(id));
    }

}