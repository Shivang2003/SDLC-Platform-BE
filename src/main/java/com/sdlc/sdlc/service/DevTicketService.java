package com.sdlc.sdlc.service;


import com.sdlc.sdlc.dto.CreateDevTicketRequest;
import com.sdlc.sdlc.dto.EditDevTicketRequest;
import com.sdlc.sdlc.entity.*;
import com.sdlc.sdlc.enums.Priority;
import com.sdlc.sdlc.enums.ProjectRoles;
import com.sdlc.sdlc.enums.TicketStatus;
import com.sdlc.sdlc.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Service
@Slf4j
public class DevTicketService {

    @Autowired
    DevTicketRepository devTicketRepository;

    @Autowired
    IdGeneratorService idGeneratorService;

    @Autowired
    DevTicketRevisionRepository devTicketRevisionRepository;

    @Autowired
    DevTicketStatusHistoryRepository devTicketStatusHistoryRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    ProjectMemberRepository projectMemberRepository;

    @Autowired
    FeatureService featureService;

    @Autowired
    TestTicketService testTicketService;

    @Autowired
    OrganizationMemberRepository organizationMemberRepository;

    @Autowired
    TestTicketRepository testTicketRepository;

    public DevTicket checkIfDevTicketNameExists(String name, Feature feature){
        try{
            DevTicket devTicket = devTicketRepository.findByTitleAndFeature(name, feature);
            return devTicket;
        }
        catch (Exception e){
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error checking if feature exists", e);
        }
    }

    public DevTicket checkIfDevTicketKeyExists(String devTicketId){
        try{
            DevTicket devTicket = devTicketRepository.findByKey(devTicketId);
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
            devTicket.setAssignee(devTicket.getAssignee());

            devTicket.setStatus(devTicket.getStatus() != null ? devTicket.getStatus() : TicketStatus.TODO);
            devTicket.setPriority(devTicket.getPriority() != null ? devTicket.getPriority() : Priority.LOW);
            devTicket.setCurrentRevision(1);


            devTicket.setCreatedAt(now);
            devTicket.setUpdatedAt(now);
            devTicket.setUpdatedBy(createdBy);

            DevTicket savedTicket = devTicketRepository.save(devTicket);

            createRevision(savedTicket, createdBy);

            return savedTicket;
        }
        catch (Exception e) {
            log.error("Error creating revision for DevTicket {}", devTicket.getKey(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to create DevTicket revision", e);
        }
    }

    public DevTicket mapToDevTicket(CreateDevTicketRequest request, Feature feature) {

        DevTicket devTicket = new DevTicket();
        devTicket.setTitle(request.getTitle());
        devTicket.setDescription(request.getDescription());
        devTicket.setStatus(request.getStatus());
        devTicket.setPriority(request.getPriority());
        devTicket.setTargetVersionId(request.getTargetVersionId());

        if (request.getAssignee() != null && !request.getAssignee().isBlank()) {

            User assignedUser = userRepository.findByUserName(request.getAssignee());
            if (assignedUser == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Assignee user not found");
            }
            List<ProjectMember> projectMembers = projectMemberRepository.findByProject_Id(feature.getProject().getId());
            ProjectMember assignee = projectMembers.stream()
                    .filter(member ->
                            member.getOrganizationMember() != null
                                    && member.getOrganizationMember().getUser() != null
                                    && member.getOrganizationMember().getUser()
                                    .getId().equals(assignedUser.getId()))
                    .findFirst()
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.BAD_REQUEST,
                            "Assignee is not a member of this project"
                    ));

            devTicket.setAssignee(assignee);
        }
        else{
            return null;
        }

        return devTicket;
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

    public DevTicket updateDevTicketStatus(
            DevTicket devTicket,
            TicketStatus newStatus,
            ProjectMember changedBy) {

        try {
            TicketStatus previousStatus = devTicket.getStatus();

            if (previousStatus == newStatus) {
                return devTicket;
            }

            LocalDateTime now = LocalDateTime.now();

            // Update ticket
            devTicket.setStatus(newStatus);
            devTicket.setUpdatedAt(now);
            devTicket.setCurrentRevision(
                    devTicket.getCurrentRevision() + 1
            );

            DevTicket updatedTicket = devTicketRepository.save(devTicket);

            // Create status history
            DevTicketStatusHistory history = new DevTicketStatusHistory();

            history.setDevTicket(updatedTicket);
            history.setPreviousStatus(previousStatus);
            history.setNewStatus(newStatus);
            history.setChangedBy(changedBy);
            history.setChangedAt(now);

            devTicketStatusHistoryRepository.save(history);

            // Create revision snapshot
            createRevision(updatedTicket, changedBy);

            return updatedTicket;

        } catch (Exception e) {
            log.error(
                    "Error updating status for DevTicket {}",
                    devTicket.getKey(),
                    e
            );

            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Failed to update DevTicket status",
                    e
            );
        }
    }

    public DevTicket editDevTicket(
            String ticketKey,
            EditDevTicketRequest request,
            String userName) {

        DevTicket devTicket = devTicketRepository.findByKey(ticketKey);

        if (devTicket == null) {
            throw new RuntimeException("DevTicket not found");
        }

        // Validate edit access
        featureService.validateFeatureEditAccess(devTicket.getProject().getProjectId(), userName, Set.of(ProjectRoles.TESTER,ProjectRoles.VIEWER));

        // Find the logged-in user
        User user = userRepository.findByUserName(userName);

        if (user == null) {
            throw new RuntimeException("User not found");
        }

        // Find the user's membership in this project's organization
        OrganizationMember organizationMember = organizationMemberRepository.findByUserAndOrganization(user, devTicket.getProject().getOrganization());

        if (organizationMember == null) {
            throw new RuntimeException("Organization member not found");
        }

        // Find the user's membership in this specific project
        ProjectMember updatedBy = projectMemberRepository.findByProjectAndOrganizationMember(devTicket.getProject(),organizationMember)
                .orElseThrow(() -> new RuntimeException("Project member not found"));

        if (request.getTestTicketStatuses() != null
                && !request.getTestTicketStatuses().isEmpty()) {

            testTicketService.updateExecutionStatusesForTestTicket(
                    devTicket.getKey(),
                    request.getTestTicketStatuses(),
                    updatedBy
            );
        }

        // Update title
        if (request.getTitle() != null) {
            devTicket.setTitle(request.getTitle());
        }

        // Update description
        if (request.getDescription() != null) {
            devTicket.setDescription(request.getDescription());
        }

        // Update priority
        if (request.getPriority() != null) {
            devTicket.setPriority(request.getPriority());
        }

        // Update assignee
        if (request.getAssigneeUserName() != null) {
            User assigneeUser = userRepository.findByUserName(request.getAssigneeUserName());
            if (assigneeUser == null) {
                throw new RuntimeException("Assignee user not found");
            }
            OrganizationMember assigneeOrganizationMember = organizationMemberRepository.findByUserAndOrganization(assigneeUser, devTicket.getProject().getOrganization());
            if (assigneeOrganizationMember == null) {
                throw new RuntimeException("Assignee is not a member of this organization");
            }
            ProjectMember assignee = projectMemberRepository.findByProjectAndOrganizationMember(
                            devTicket.getProject(),
                            assigneeOrganizationMember).orElseThrow(() -> new RuntimeException("Assignee is not a member of this project"));
            devTicket.setAssignee(assignee);
        }

        // Update audit information
        devTicket.setUpdatedBy(updatedBy);
        devTicket.setUpdatedAt(LocalDateTime.now());

        // Increment revision
        if (devTicket.getCurrentRevision() == null) {
            devTicket.setCurrentRevision(1);
        } else {
            devTicket.setCurrentRevision(
                    devTicket.getCurrentRevision() + 1
            );
        }

        // Save the updated ticket
        DevTicket updatedTicket = devTicketRepository.save(devTicket);

        // Create a snapshot of the new revision
        createRevision(updatedTicket, updatedBy);

        return updatedTicket;
    }
}
