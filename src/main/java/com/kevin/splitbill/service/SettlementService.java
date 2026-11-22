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
public class SettlementService {

    private static final String GITHUB_USERNAME = "kevinaxeln";

    private final BillGroupRepository billGroupRepository;
    private final ParticipantRepository participantRepository;
    private final ExpenseRepository expenseRepository;

    public SettlementService(
            BillGroupRepository billGroupRepository,
            ParticipantRepository participantRepository,
            ExpenseRepository expenseRepository
    ) {
        this.billGroupRepository = billGroupRepository;
        this.participantRepository = participantRepository;
        this.expenseRepository = expenseRepository;
    }

    private int calculateServiceChargePct() {
        return GITHUB_USERNAME.chars().sum() % 10;
    }

    @Transactional(readOnly = true)
    public SettlementResponse calculateSettlement(Long groupId) {

        BillGroup group = billGroupRepository.findById(groupId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Group not found"
                ));

        List<Participant> participants =
                participantRepository.findByBillGroupId(groupId);

        List<Expense> expenses =
                expenseRepository.findByBillGroupId(groupId);

        Map<Long, BigDecimal> balances = new LinkedHashMap<>();

        participants.forEach(participant ->
                balances.put(participant.getId(), BigDecimal.ZERO)
        );

        BigDecimal totalExpenses = BigDecimal.ZERO;

        // Calculate each participant's balance.
        for (Expense expense : expenses) {

            totalExpenses = totalExpenses.add(expense.getAmount());

            Long payerId = expense.getPaidBy().getId();

            // The payer gets credit for the amount paid.
            balances.merge(
                    payerId,
                    expense.getAmount(),
                    BigDecimal::add
            );

            // Each participant is responsible for their split.
            for (ExpenseSplit split : expense.getSplits()) {

                Long participantId = split.getParticipant().getId();

                balances.merge(
                        participantId,
                        split.getAmount().negate(),
                        BigDecimal::add
                );
            }
        }

        // Calculate service charge from GitHub username.
        int serviceChargePct = calculateServiceChargePct();

        BigDecimal serviceChargeAmount = totalExpenses
                .multiply(BigDecimal.valueOf(serviceChargePct))
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        // Separate debtors and creditors.
        List<Long> debtors = new ArrayList<>();
        List<Long> creditors = new ArrayList<>();

        for (Map.Entry<Long, BigDecimal> entry : balances.entrySet()) {
            if (entry.getValue().signum() < 0) {
                debtors.add(entry.getKey());
            } else if (entry.getValue().signum() > 0) {
                creditors.add(entry.getKey());
            }
        }

        Map<Long, Participant> participantMap = new HashMap<>();

        participants.forEach(p -> participantMap.put(p.getId(), p));

        List<SettlementTransferResponse> transfers = new ArrayList<>();

        int debtorIndex = 0;
        int creditorIndex = 0;

        while (debtorIndex < debtors.size()
                && creditorIndex < creditors.size()) {

            Long debtorId = debtors.get(debtorIndex);
            Long creditorId = creditors.get(creditorIndex);

            BigDecimal debt = balances.get(debtorId).abs();
            BigDecimal credit = balances.get(creditorId);

            BigDecimal transferAmount = debt.min(credit);

            transfers.add(new SettlementTransferResponse(
                    debtorId,
                    participantMap.get(debtorId).getName(),
                    creditorId,
                    participantMap.get(creditorId).getName(),
                    transferAmount
            ));

            balances.put(
                    debtorId,
                    balances.get(debtorId).add(transferAmount)
            );

            balances.put(
                    creditorId,
                    balances.get(creditorId).subtract(transferAmount)
            );

            if (balances.get(debtorId).signum() == 0) {
                debtorIndex++;
            }

            if (balances.get(creditorId).signum() == 0) {
                creditorIndex++;
            }
        }

        return new SettlementResponse(
                group.getId(),
                group.getName(),
                totalExpenses,
                serviceChargePct,
                serviceChargeAmount,
                transfers
        );
    }
}
