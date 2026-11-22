
package com.kevin.splitbill.service;

import com.kevin.splitbill.dto.SettlementResponse;
import com.kevin.splitbill.dto.SettlementTransferResponse;
import com.kevin.splitbill.entity.*;
import com.kevin.splitbill.repository.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SettlementServiceTest {

    @Mock
    private BillGroupRepository billGroupRepository;

    @Mock
    private ParticipantRepository participantRepository;

    @Mock
    private ExpenseRepository expenseRepository;

    @InjectMocks
    private SettlementService settlementService;

    @Test
    void calculateSettlement_shouldReturnCorrectTransfers() {

        // Arrange: Create group
        BillGroup group = new BillGroup("Bali Trip");
        group.setId(1L);

        // Create participants
        Participant kevin = new Participant();
        kevin.setId(1L);
        kevin.setName("Kevin");

        Participant andi = new Participant();
        andi.setId(2L);
        andi.setName("Andi");

        Participant budi = new Participant();
        budi.setId(3L);
        budi.setName("Budi");

        // Create expense: Kevin pays Rp300,000
        Expense expense = new Expense();
        expense.setId(1L);
        expense.setDescription("Dinner");
        expense.setAmount(new BigDecimal("300000"));
        expense.setPaidBy(kevin);
        expense.setBillGroup(group);

        // Each participant owes Rp100,000
        ExpenseSplit split1 = new ExpenseSplit();
        split1.setParticipant(kevin);
        split1.setAmount(new BigDecimal("100000"));

        ExpenseSplit split2 = new ExpenseSplit();
        split2.setParticipant(andi);
        split2.setAmount(new BigDecimal("100000"));

        ExpenseSplit split3 = new ExpenseSplit();
        split3.setParticipant(budi);
        split3.setAmount(new BigDecimal("100000"));

        expense.addSplit(split1);
        expense.addSplit(split2);
        expense.addSplit(split3);

        // Mock repository responses
        when(billGroupRepository.findById(1L))
                .thenReturn(Optional.of(group));

        when(participantRepository.findByBillGroupId(1L))
                .thenReturn(List.of(kevin, andi, budi));

        when(expenseRepository.findByBillGroupId(1L))
                .thenReturn(List.of(expense));

        // Act: Calculate settlement
        SettlementResponse result =
                settlementService.calculateSettlement(1L);

        // Assert: Verify results
        assertEquals(1L, result.groupId());
        assertEquals("Bali Trip", result.groupName());

        assertEquals(
                0,
                result.totalExpenses().compareTo(
                        new BigDecimal("300000")
                )
        );

        assertEquals(7, result.serviceChargePct());

        assertEquals(
                0,
                result.serviceChargeAmount().compareTo(
                        new BigDecimal("21000")
                )
        );

        assertEquals(2, result.transfers().size());

        SettlementTransferResponse transfer1 =
                result.transfers().get(0);

        assertEquals("Andi", transfer1.fromParticipantName());
        assertEquals("Kevin", transfer1.toParticipantName());
        assertEquals(
                0,
                transfer1.amount().compareTo(new BigDecimal("100000"))
        );

        SettlementTransferResponse transfer2 =
                result.transfers().get(1);

        assertEquals("Budi", transfer2.fromParticipantName());
        assertEquals("Kevin", transfer2.toParticipantName());
        assertEquals(
                0,
                transfer2.amount().compareTo(new BigDecimal("100000"))
        );

        verify(billGroupRepository).findById(1L);
        verify(participantRepository).findByBillGroupId(1L);
        verify(expenseRepository).findByBillGroupId(1L);
    }
}
