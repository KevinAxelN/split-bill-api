
package com.kevin.splitbill.controller;

import com.kevin.splitbill.dto.CreateExpenseRequest;
import com.kevin.splitbill.dto.ExpenseResponse;
import com.kevin.splitbill.service.ExpenseService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/groups/{groupId}/expenses")
public class ExpenseController {

    private final ExpenseService expenseService;

    public ExpenseController(ExpenseService expenseService) {
        this.expenseService = expenseService;
    }

    @PostMapping
    public ResponseEntity<ExpenseResponse> createExpense(
            @PathVariable Long groupId,
            @Valid @RequestBody CreateExpenseRequest request
    ) {
        ExpenseResponse response =
                expenseService.createExpense(groupId, request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<ExpenseResponse>> getExpenses(
            @PathVariable Long groupId
    ) {
        return ResponseEntity.ok(
                expenseService.getExpensesByGroupId(groupId)
        );
    }
}
