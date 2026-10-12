package com.sdlc.sdlc.service;


import com.sdlc.sdlc.entity.*;
import com.sdlc.sdlc.enums.Priority;
import com.sdlc.sdlc.enums.TicketStatus;
import com.sdlc.sdlc.repository.DevTicketRepository;
import com.sdlc.sdlc.repository.DevTicketRevisionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

@Service
@Slf4j
public class DevTicketService {

    @Autowired
    DevTicketRepository devTicketRepository;

    @Autowired
    IdGeneratorService idGeneratorService;

    @Autowired
    DevTicketRevisionRepository devTicketRevisionRepository;

    public DevTicket checkIfDevTicketNameExists(String name, Feature feature){
        try{
            DevTicket devTicket = devTicketRepository.findByNameAndFeature(name, feature);
            return devTicket;
        }
        catch (Exception e){
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error checking if feature exists", e);
        }
    }

    public DevTicket createDevTicket(DevTicket devTicket, Feature feature, ProjectMember createdBy) {

        try{
            LocalDateTime now = LocalDateTime.now();

            devTicket.setKey(idGeneratorService.generateSequenceId("devTicket","DEV"));
            devTicket.setFeature(feature);
            devTicket.setProject(feature.getProject());
            devTicket.setCreatedBy(createdBy);

            devTicket.setStatus(TicketStatus.TODO);
            devTicket.setPriority(Priority.MEDIUM);
            devTicket.setCurrentRevision(1);

            devTicket.setCreatedAt(now);
            devTicket.setUpdatedAt(now);

            DevTicket savedTicket = devTicketRepository.save(devTicket);

            createRevision(savedTicket, createdBy);

            return savedTicket;
        }
        catch (Exception e) {
            log.error("Error creating revision for DevTicket {}", devTicket.getKey(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to create DevTicket revision", e);
        }
    }

    public DevTicketRevision createRevision(DevTicket devTicket, ProjectMember changedBy) {
        try {
            DevTicketRevision revision = new DevTicketRevision();

            revision.setTicketKey(devTicket.getKey());
            revision.setRevisionNumber(devTicket.getCurrentRevision());
            revision.setDevTicket(devTicket);
            revision.setTitle(devTicket.getTitle());
            revision.setDescription(devTicket.getDescription());
            revision.setProject(devTicket.getProject());
            revision.setFeature(devTicket.getFeature());
            revision.setAssignee(devTicket.getAssignee());
            revision.setCreatedBy(devTicket.getCreatedBy());
            revision.setStatus(devTicket.getStatus());
            revision.setPriority(devTicket.getPriority());
            revision.setTargetVersionId(devTicket.getTargetVersionId());
            revision.setChangedBy(changedBy);
            revision.setCreatedAt(LocalDateTime.now());

            return devTicketRevisionRepository.save(revision);

        } catch (Exception e) {log.error("Error creating revision for DevTicket {}", devTicket.getKey(), e);throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to create DevTicket revision", e);
        }
    }
}
