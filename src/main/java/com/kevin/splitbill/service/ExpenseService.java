package com.kevin.splitbill.service;

import com.kevin.splitbill.dto.*;
import com.kevin.splitbill.entity.*;
import com.kevin.splitbill.repository.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@Service
public class ExpenseService {

    private final BillGroupRepository billGroupRepository;
    private final ParticipantRepository participantRepository;
    private final ExpenseRepository expenseRepository;

    public ExpenseService(
            BillGroupRepository billGroupRepository,
            ParticipantRepository participantRepository,
            ExpenseRepository expenseRepository
    ) {
        this.billGroupRepository = billGroupRepository;
        this.participantRepository = participantRepository;
        this.expenseRepository = expenseRepository;
    }

    @Transactional
    public ExpenseResponse createExpense(
            Long groupId,
            CreateExpenseRequest request
    ) {
        BillGroup group = billGroupRepository.findById(groupId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Group not found"
                ));

        List<Participant> participants =
                participantRepository.findByBillGroupId(groupId);

        Map<Long, Participant> participantMap = new HashMap<>();
        participants.forEach(p -> participantMap.put(p.getId(), p));

        Participant paidBy = participantMap.get(
                request.paidByParticipantId()
        );

        if (paidBy == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Payer does not belong to this group"
            );
        }

        List<Long> splitIds = request.splitParticipantIds();

        if (new HashSet<>(splitIds).size() != splitIds.size()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Duplicate split participant IDs are not allowed"
            );
        }

        List<Participant> splitParticipants = splitIds.stream()
                .map(participantId -> {
                    Participant participant = participantMap.get(participantId);

                    if (participant == null) {
                        throw new ResponseStatusException(
                                HttpStatus.BAD_REQUEST,
                                "Split participant does not belong to this group"
                        );
                    }

                    return participant;
                })
                .toList();

        Expense expense = new Expense();
        expense.setDescription(request.description().trim());
        expense.setAmount(request.amount());
        expense.setBillGroup(group);
        expense.setPaidBy(paidBy);

        // Divide amount equally, keeping the total exact to the cent.
        BigDecimal baseAmount = request.amount()
                .divide(
                        BigDecimal.valueOf(splitParticipants.size()),
                        2,
                        RoundingMode.DOWN
                );

        BigDecimal remainder = request.amount()
                .subtract(baseAmount.multiply(
                        BigDecimal.valueOf(splitParticipants.size())
                ));

        for (int i = 0; i < splitParticipants.size(); i++) {
            BigDecimal splitAmount = baseAmount;

            if (i == 0) {
                splitAmount = splitAmount.add(remainder);
            }

            ExpenseSplit split = new ExpenseSplit();
            split.setParticipant(splitParticipants.get(i));
            split.setAmount(splitAmount);

            expense.addSplit(split);
        }

        Expense savedExpense = expenseRepository.save(expense);

        return mapToResponse(savedExpense);
    }

    private ExpenseResponse mapToResponse(Expense expense) {
        List<ExpenseSplitResponse> splits = expense.getSplits()
                .stream()
                .map(split -> new ExpenseSplitResponse(
                        split.getParticipant().getId(),
                        split.getParticipant().getName(),
                        split.getAmount()
                ))
                .toList();

        return new ExpenseResponse(
                expense.getId(),
                expense.getDescription(),
                expense.getAmount(),
                expense.getPaidBy().getId(),
                expense.getPaidBy().getName(),
                splits
        );
    }

    @Transactional(readOnly = true)
    public List<ExpenseResponse> getExpensesByGroupId(Long groupId) {
        if (!billGroupRepository.existsById(groupId)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Group not found"
            );
        }

        return expenseRepository.findByBillGroupId(groupId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }
}
