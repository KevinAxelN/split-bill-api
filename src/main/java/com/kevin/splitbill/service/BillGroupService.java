package com.kevin.splitbill.service;

import com.kevin.splitbill.dto.BillGroupResponse;
import com.kevin.splitbill.dto.CreateBillGroupRequest;
import com.kevin.splitbill.dto.ParticipantResponse;
import com.kevin.splitbill.entity.BillGroup;
import com.kevin.splitbill.entity.Participant;
import com.kevin.splitbill.repository.BillGroupRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class BillGroupService {

    private final BillGroupRepository billGroupRepository;

    public BillGroupService(BillGroupRepository billGroupRepository) {
        this.billGroupRepository = billGroupRepository;
    }

    @Transactional
    public BillGroupResponse createGroup(CreateBillGroupRequest request) {

        BillGroup billGroup = new BillGroup(request.name().trim());

        request.participants().forEach(name -> {
            Participant participant = new Participant();
            participant.setName(name.trim());
            billGroup.addParticipant(participant);
        });

        BillGroup savedGroup = billGroupRepository.save(billGroup);

        return new BillGroupResponse(
                savedGroup.getId(),
                savedGroup.getName(),
                savedGroup.getParticipants()
                        .stream()
                        .map(participant -> new ParticipantResponse(
                                participant.getId(),
                                participant.getName()
                        ))
                        .toList()
        );
    }

    @Transactional(readOnly = true)
    public List<BillGroupResponse> getAllGroups() {
        return billGroupRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public BillGroupResponse getGroupById(Long id) {
        BillGroup billGroup = billGroupRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Group not found with id: " + id)
                );

        return mapToResponse(billGroup);
    }

    private BillGroupResponse mapToResponse(BillGroup billGroup) {
        return new BillGroupResponse(
                billGroup.getId(),
                billGroup.getName(),
                billGroup.getParticipants()
                        .stream()
                        .map(participant -> new ParticipantResponse(
                                participant.getId(),
                                participant.getName()
                        ))
                        .toList()
        );
    }
}
