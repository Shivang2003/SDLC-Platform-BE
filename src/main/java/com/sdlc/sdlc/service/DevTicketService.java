package com.sdlc.sdlc.service;


import com.sdlc.sdlc.dto.CreateDevTicketRequest;
import com.sdlc.sdlc.dto.DevTicketRevisionResponse;
import com.sdlc.sdlc.dto.EditDevTicketRequest;
import com.sdlc.sdlc.entity.*;
import com.sdlc.sdlc.enums.Priority;
import com.sdlc.sdlc.enums.ProjectRoles;
import com.sdlc.sdlc.enums.TicketStatus;
import com.sdlc.sdlc.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.*;

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
    FeatureRepositroy featureRepositroy;

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
            String devTicketId = idGeneratorService.generateSequenceId("devTicket","DEV");
            devTicket.setKey(devTicketId);
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

            Feature featureContatiningDevTicket = featureRepositroy.findByFeatureId(feature.getFeatureId());
            if (featureContatiningDevTicket.getDevTicketId() == null) {
                featureContatiningDevTicket.setDevTicketId(new ArrayList<>());
            }
            featureContatiningDevTicket.getDevTicketId().add(devTicketId);
            featureRepositroy.save(featureContatiningDevTicket);

            DevTicket savedTicket = devTicketRepository.save(devTicket);

            createRevision(
                    savedTicket,
                    createdBy,
                    List.of(),
                    Map.of()
            );

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

    public DevTicketRevision createRevision(
            DevTicket devTicket,
            ProjectMember changedBy,
            List<String> changedFields,
            Map<String,FieldChange> fieldChanges) {

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
            revision.setChangedFields(new ArrayList<>(changedFields));
            revision.setChanges(fieldChanges);
            revision.setCreatedAt(LocalDateTime.now());

            return devTicketRevisionRepository.save(revision);

        } catch (Exception e) {
            log.error(
                    "Error creating revision for DevTicket {}",
                    devTicket.getKey(),
                    e
            );

            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Failed to create DevTicket revision",
                    e
            );
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

            Map<String,FieldChange> statusChanges = new HashMap<>();
            statusChanges.put("status",new FieldChange(devTicket.getStatus(),newStatus));

            // Update ticket
            devTicket.setStatus(newStatus);
            devTicket.setUpdatedBy(changedBy);
            devTicket.setUpdatedAt(now);

            if (devTicket.getCurrentRevision() == null) {
                devTicket.setCurrentRevision(1);
            } else {
                devTicket.setCurrentRevision(
                        devTicket.getCurrentRevision() + 1
                );
            }

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
            createRevision(
                    updatedTicket,
                    changedBy,
                    List.of("status"),
                    statusChanges
            );

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

        featureService.validateFeatureEditAccess(
                devTicket.getProject().getProjectId(),
                userName,
                Set.of(ProjectRoles.TESTER, ProjectRoles.VIEWER)
        );

        User user = userRepository.findByUserName(userName);

        if (user == null) {
            throw new RuntimeException("User not found");
        }

        OrganizationMember organizationMember =
                organizationMemberRepository.findByUserAndOrganization(
                        user,
                        devTicket.getProject().getOrganization()
                );

        if (organizationMember == null) {
            throw new RuntimeException("Organization member not found");
        }

        ProjectMember updatedBy =
                projectMemberRepository.findByProjectAndOrganizationMember(
                        devTicket.getProject(),
                        organizationMember
                ).orElseThrow(() ->
                        new RuntimeException("Project member not found"));

        // Update linked TestTicket statuses separately
        if (request.getTestTicketStatuses() != null
                && !request.getTestTicketStatuses().isEmpty()) {

            testTicketService.updateExecutionStatusesForTestTicket(
                    devTicket.getKey(),
                    request.getTestTicketStatuses(),
                    updatedBy
            );
        }

        List<String> changedFields = new ArrayList<>();
        Map<String,FieldChange> fieldChanges = new HashMap<>();

        // Update title
        if (request.getTitle() != null
                && !Objects.equals(devTicket.getTitle(), request.getTitle())) {
            fieldChanges.put("title",new FieldChange(devTicket.getTitle(),request.getTitle()));
            devTicket.setTitle(request.getTitle());
            changedFields.add("title");
        }

        // Update description
        if (request.getDescription() != null
                && !Objects.equals(
                devTicket.getDescription(),
                request.getDescription())) {
            fieldChanges.put("description",new FieldChange(devTicket.getDescription(),request.getDescription()));
            devTicket.setDescription(request.getDescription());
            changedFields.add("description");
        }

        // Update priority
        if (request.getPriority() != null
                && !Objects.equals(
                devTicket.getPriority(),
                request.getPriority())) {
            fieldChanges.put("priority",new FieldChange(devTicket.getPriority(),request.getPriority()));
            devTicket.setPriority(request.getPriority());
            changedFields.add("priority");
        }

        // Update assignee
        if (request.getAssigneeUserName() != null) {

            User assigneeUser = userRepository.findByUserName(
                    request.getAssigneeUserName()
            );

            if (assigneeUser == null) {
                throw new RuntimeException("Assignee user not found");
            }

            OrganizationMember assigneeOrganizationMember =
                    organizationMemberRepository.findByUserAndOrganization(
                            assigneeUser,
                            devTicket.getProject().getOrganization()
                    );

            if (assigneeOrganizationMember == null) {
                throw new RuntimeException(
                        "Assignee is not a member of this organization"
                );
            }

            ProjectMember assignee =
                    projectMemberRepository.findByProjectAndOrganizationMember(
                            devTicket.getProject(),
                            assigneeOrganizationMember
                    ).orElseThrow(() ->
                            new RuntimeException(
                                    "Assignee is not a member of this project"
                            ));

            ObjectId currentAssigneeId = devTicket.getAssignee() != null
                    ? devTicket.getAssignee().getId()
                    : null;

            ObjectId newAssigneeId = assignee.getId();

            if (!Objects.equals(currentAssigneeId, newAssigneeId)) {
                fieldChanges.put("assignee",new FieldChange(assignee,request.getAssigneeUserName()));
                devTicket.setAssignee(assignee);
                changedFields.add("assignee");
            }
        }

        // No DevTicket fields changed: do not create a revision
        if (changedFields.isEmpty()) {
            return devTicket;
        }

        // Update audit information
        devTicket.setUpdatedBy(updatedBy);
        devTicket.setUpdatedAt(LocalDateTime.now());

        // Increment revision only when fields actually change
        if (devTicket.getCurrentRevision() == null) {
            devTicket.setCurrentRevision(1);
        } else {
            devTicket.setCurrentRevision(
                    devTicket.getCurrentRevision() + 1
            );
        }

        // Save updated ticket
        DevTicket updatedTicket = devTicketRepository.save(devTicket);

        // Create one snapshot containing all changed fields
        createRevision(updatedTicket, updatedBy, changedFields, fieldChanges);

        return updatedTicket;
    }



//    public List<DevTicketRevisionResponse> getDevTicketRevisions(
//            String ticketKey) {
//
//        DevTicket devTicket = devTicketRepository.findByKey(ticketKey);
//
//        if (devTicket == null) {
//            throw new RuntimeException("DevTicket not found");
//        }
//
//        List<DevTicketRevision> revisions = devTicketRevisionRepository.findByTicketKeyOrderByRevisionNumberDesc(ticketKey);
//
//
//        return revisions.stream().map(revision -> {
//            DevTicketRevisionResponse response =
//                    new DevTicketRevisionResponse();
//
//            response.setTicketKey(revision.getTicketKey());
//            response.setRevisionNumber(revision.getRevisionNumber());
//            response.setTitle(revision.getTitle());
//            response.setDescription(revision.getDescription());
//
//            if (revision.getProject() != null) {
//                response.setProjectId(
//                        revision.getProject().getProjectId());
//            }
//
//            if (revision.getFeature() != null) {
//                response.setFeatureId(
//                        revision.getFeature().getFeatureId());
//            }
//
//            if (revision.getAssignee() != null
//                    && revision.getAssignee().getOrganizationMember() != null
//                    && revision.getAssignee().getOrganizationMember().getUser() != null) {
//                response.setAssigneeUserName(
//                        revision.getAssignee().getOrganizationMember()
//                                .getUser().getUserName());
//            }
//
//            if (revision.getCreatedBy() != null
//                    && revision.getCreatedBy().getOrganizationMember() != null
//                    && revision.getCreatedBy().getOrganizationMember().getUser() != null) {
//                response.setCreatedByUserName(
//                        revision.getCreatedBy().getOrganizationMember()
//                                .getUser().getUserName());
//            }
//
//            response.setStatus(revision.getStatus());
//            response.setPriority(revision.getPriority());
//
//            if (revision.getChangedBy() != null
//                    && revision.getChangedBy().getOrganizationMember() != null
//                    && revision.getChangedBy().getOrganizationMember().getUser() != null) {
//                response.setChangedByUserName(
//                        revision.getChangedBy().getOrganizationMember()
//                                .getUser().getUserName());
//            }
//
//            response.setCreatedAt(revision.getCreatedAt());
//
//            return response;
//        }).toList();
//    }


    public List<DevTicketRevisionResponse> getDevTicketRevisions(String ticketKey) {

        long start = System.currentTimeMillis();

        DevTicket devTicket = devTicketRepository.findByKey(ticketKey);

        System.out.println("Find DevTicket: " +
                (System.currentTimeMillis() - start) + " ms");

        if (devTicket == null) {
            throw new RuntimeException("DevTicket not found");
        }

        long queryStart = System.currentTimeMillis();

        List<DevTicketRevision> revisions =
                devTicketRevisionRepository
                        .findByTicketKeyOrderByRevisionNumberDesc(ticketKey);

        System.out.println("Fetch revisions: " +
                (System.currentTimeMillis() - queryStart) + " ms");

        long mapStart = System.currentTimeMillis();

        List<DevTicketRevisionResponse> result = revisions.stream().map(revision -> {
            DevTicketRevisionResponse response = new DevTicketRevisionResponse();

            response.setTicketKey(revision.getTicketKey());
            response.setRevisionNumber(revision.getRevisionNumber());
            response.setTitle(revision.getTitle());
            response.setDescription(revision.getDescription());

            if (revision.getProject() != null) {
                response.setProjectId(revision.getProject().getProjectId());
            }

            if (revision.getFeature() != null) {
                response.setFeatureId(revision.getFeature().getFeatureId());
            }

            if (revision.getAssignee() != null
                    && revision.getAssignee().getOrganizationMember() != null
                    && revision.getAssignee().getOrganizationMember().getUser() != null) {
                response.setAssigneeUserName(
                        revision.getAssignee().getOrganizationMember()
                                .getUser().getUserName());
            }

            if (revision.getCreatedBy() != null
                    && revision.getCreatedBy().getOrganizationMember() != null
                    && revision.getCreatedBy().getOrganizationMember().getUser() != null) {
                response.setCreatedByUserName(
                        revision.getCreatedBy().getOrganizationMember()
                                .getUser().getUserName());
            }
            response.setChangedFields(revision.getChangedFields());
            response.setChanges(revision.getChanges());
            response.setStatus(revision.getStatus());
            response.setPriority(revision.getPriority());

            if (revision.getChangedBy() != null
                    && revision.getChangedBy().getOrganizationMember() != null
                    && revision.getChangedBy().getOrganizationMember().getUser() != null) {
                response.setChangedByUserName(
                        revision.getChangedBy().getOrganizationMember()
                                .getUser().getUserName());
            }

            response.setCreatedAt(revision.getCreatedAt());

            return response;
        }).toList();

        System.out.println("Map revisions: " +
                (System.currentTimeMillis() - mapStart) + " ms");

        System.out.println("Total service time: " +
                (System.currentTimeMillis() - start) + " ms");

        return result;
    }

}
