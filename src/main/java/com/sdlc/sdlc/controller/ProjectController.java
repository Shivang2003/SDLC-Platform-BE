package com.sdlc.sdlc.controller;

import com.sdlc.sdlc.dto.*;
import com.sdlc.sdlc.entity.*;
import com.sdlc.sdlc.enums.ProjectRoles;
import com.sdlc.sdlc.enums.TestExecutionStatus;
import com.sdlc.sdlc.enums.TicketStatus;
import com.sdlc.sdlc.repository.*;
import com.sdlc.sdlc.service.BugTicketService;
import com.sdlc.sdlc.service.FeatureService;
import com.sdlc.sdlc.service.ProjectService;
import com.sdlc.sdlc.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Set;

@Controller
@RequestMapping("/project")
@Slf4j
public class ProjectController {

    @Autowired
    ProjectService projectService;

    @Autowired
    FeatureService featureService;

    @Autowired
    UserService userService;

    @Autowired
    OrganizationMemberRepository organizationMemberRepository;

    @Autowired
    ProjectMemberRepository projectMemberRepository;

    @Autowired
    ProjectRepository projectRepository;

    @Autowired
    FeatureRepositroy featureRepository;

    @Autowired
    DevTicketRepository devTicketRepository;

    @Autowired
    TestTicketRepository testTicketRepository;

    @Autowired
    BugTicketService bugTicketService;

    @Autowired
    BugTicketRepository bugTicketRepository;

    @Autowired
    UserRepository userRepository;

    @PostMapping("/{projectId}/create-feature")
    public ResponseEntity<?> createFeature(@PathVariable String projectId, @RequestBody Feature feature) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userName = authentication.getName();
            ProjectMember projectMember = featureService.validateFeatureEditAccess(projectId, userName, Set.of(ProjectRoles.VIEWER));
            if (projectMember == null) {
                return new ResponseEntity<>(new ErrorResponse("User is not a member of the project"), HttpStatus.FORBIDDEN);
            }
            Project projectInDb = projectService.findProjectById(projectId);
            if (projectInDb == null) {
                return new ResponseEntity<>(new ErrorResponse("Project doesn't exist"), HttpStatus.NOT_FOUND);
            }
            Feature checkFeature = featureService.checkIfFeatureNameExists(feature.getName(), projectInDb);
            if (checkFeature != null) {
                return new ResponseEntity<>(new ErrorResponse("Feature already exists"), HttpStatus.CONFLICT);
            }
            featureService.createNewFeature(feature, projectInDb, projectMember);

            return new ResponseEntity<>(feature, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error creating feature: ", e);
            return ResponseEntity.badRequest().body("Error creating feature");
        }
    }

    @GetMapping("/{projectId}/features")
    public ResponseEntity<?> getAllFeatures(@PathVariable String projectId) {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        String currentUserName = authentication.getName();

        Project project = projectRepository.findByProjectId(projectId);

        if (project == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Project not found");
        }

        try {
            featureService.validateFeatureEditAccess(
                    projectId,
                    currentUserName,
                    Set.of(ProjectRoles.VIEWER)
            );

            List<Feature> features = featureRepository.findByProject(project);

            List<FeatureSummaryResponse> response = features.stream()
                    .map(feature -> new FeatureSummaryResponse(
                            feature.getFeatureId(),
                            feature.getName(),
                            feature.getDescription()
                    ))
                    .toList();

            if (response.isEmpty()) {
                return new ResponseEntity<>(new ErrorResponse("no features available"), HttpStatus.FORBIDDEN);
            }
            return new ResponseEntity<>(response, HttpStatus.OK);

        } catch (AccessDeniedException e) {
            return new ResponseEntity<>(new ErrorResponse(e.getMessage()), HttpStatus.FORBIDDEN);
        }
    }

    @PostMapping("/{projectId}/add-member")
    public ResponseEntity<?> addUserToProject(@PathVariable String projectId, @RequestBody AddProjectMemberRequest userDetails) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String currentUserName = authentication.getName();
            Project project = projectRepository.findByProjectId(projectId);
            if (project == null) {
                return new ResponseEntity<>(new ErrorResponse("Project doesn't exist"), HttpStatus.NOT_FOUND);
            }

            User currentUser = userService.findByUserName(currentUserName);
            OrganizationMember currentOrgMember = organizationMemberRepository.findByUserAndOrganization(currentUser, project.getOrganization());

            if (currentOrgMember == null) {
                return new ResponseEntity<>(new ErrorResponse("You are not a member of this organization"), HttpStatus.FORBIDDEN);
            }
            ProjectMember currentProjectMember = projectMemberRepository.findByProjectAndOrganizationMember(project, currentOrgMember).orElse(null);

            if (currentProjectMember == null || (currentProjectMember.getRole() != ProjectRoles.ADMIN && currentProjectMember.getRole() != ProjectRoles.OWNER)) {
                return new ResponseEntity<>(new ErrorResponse("Only project admins can add members"), HttpStatus.FORBIDDEN);
            }
            User newUser = userService.findByUserName(userDetails.getUserName());

            if (newUser == null) {
                return new ResponseEntity<>(new ErrorResponse("User doesn't exist"), HttpStatus.NOT_FOUND);
            }
            OrganizationMember newOrgMember = organizationMemberRepository.findByUserAndOrganization(newUser, project.getOrganization());

            if (newOrgMember == null) {
                return new ResponseEntity<>(new ErrorResponse("User is not a member of this organization"), HttpStatus.BAD_REQUEST);
            }
            ProjectRoles role = userDetails.getRole();

//            if (!List.of("ADMIN", "VIEWER", "WORKER", "OWNER").contains(role)) {
//                return new ResponseEntity<>(new ErrorResponse("Invalid project role"), HttpStatus.BAD_REQUEST);
//            }

            ProjectMember projectMember = projectService.addMemberToProject(newOrgMember, project, role);
            return new ResponseEntity<>(projectMember, HttpStatus.CREATED);

        } catch (IllegalStateException e) {
            return new ResponseEntity<>(new ErrorResponse(e.getMessage()), HttpStatus.CONFLICT);
        } catch (Exception e) {
            return new ResponseEntity<>(new ErrorResponse("Unable to add project member"), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{projectId}/features/{featureId}/dev-tickets")
    public ResponseEntity<?> getAllDevTickets(@PathVariable String projectId, @PathVariable String featureId) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String currentUserName = authentication.getName();

            Project project = projectRepository.findByProjectId(projectId);
            if (project == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Project not found");
            }

            Feature feature = featureService.checkIfFeatureIdExists(featureId);
            if (feature == null) {
                return ResponseEntity.status(404).body("Feature doesn't exist");
            }

            List<DevTicket> devTickets = devTicketRepository.findByProjectAndFeature(project, feature);

            List<DevTicketBoardResponse.DevTicketItem> ticketItems = devTickets.stream()
                    .map(ticket -> new DevTicketBoardResponse.DevTicketItem(
                            ticket.getTitle(),
                            ticket.getPriority(),
                            ticket.getKey(),
                            ticket.getStatus(),
                            ticket.getUpdatedAt())).toList();

            DevTicketBoardResponse response = new DevTicketBoardResponse(List.of(TicketStatus.values()), ticketItems);

            return new ResponseEntity<>(response, HttpStatus.OK);

        } catch (Exception e) {
            return new ResponseEntity<>(new ErrorResponse("Unable to add project member"), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{projectId}/features/{featureId}/test-tickets")
    public ResponseEntity<?> getAllTestTickets(@PathVariable String projectId, @PathVariable String featureId) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String currentUserName = authentication.getName();

            Project project = projectRepository.findByProjectId(projectId);
            if (project == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Project not found");
            }

            Feature feature = featureService.checkIfFeatureIdExists(featureId);
            if (feature == null) {
                return ResponseEntity.status(404).body("Feature doesn't exist");
            }

            List<TestTicket> testTickets = testTicketRepository.findByProjectAndFeature(project, feature);

            List<TestTicketBoardResponse.TestTicketItem> ticketItems = testTickets.stream()
                    .map(ticket -> new TestTicketBoardResponse.TestTicketItem(
                            ticket.getTitle(),
                            ticket.getPriority(),
                            ticket.getKey(),
                            ticket.getExecutionStatus(),
                            ticket.getUpdatedAt())).toList();

            TestTicketBoardResponse response = new TestTicketBoardResponse(List.of(TestExecutionStatus.values()), ticketItems);

            return new ResponseEntity<>(response, HttpStatus.OK);

        } catch (Exception e) {
            return new ResponseEntity<>(new ErrorResponse("Unable to add project member"), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{projectId}/project-members")
    public ResponseEntity<?> getAllTestTickets(@PathVariable String projectId) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String currentUserName = authentication.getName();

            Project project = projectRepository.findByProjectId(projectId);
            if (project == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Project not found");
            }
            List<ProjectMember> projectMembers = projectMemberRepository.findByProject_Id(project.getId());
            List<String> userNames = projectMembers.stream()
                    .map(member -> member.getOrganizationMember().getUser().getUserName())
                    .toList();

            return new ResponseEntity<>(userNames, HttpStatus.OK);
        } catch (Exception e){
            return new ResponseEntity<>(new ErrorResponse("Unable to add project member"), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/{projectId}/change-roles")
    public ResponseEntity<?> changeProjectMemberRole(@PathVariable String projectId, @RequestBody ChangeProjectMemberRoleRequest request) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String currentUserName = authentication.getName();

            Project project = projectRepository.findByProjectId(projectId);
            if (project == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Project not found");
            }

            User currentUser = userService.findByUserName(currentUserName);
            OrganizationMember currentOrgMember = organizationMemberRepository.findByUserAndOrganization(currentUser, project.getOrganization());

            if (currentOrgMember == null) {
                return new ResponseEntity<>(new ErrorResponse("You are not a member of this organization"), HttpStatus.FORBIDDEN);
            }
            ProjectMember currentProjectMember = projectMemberRepository.findByProjectAndOrganizationMember(project, currentOrgMember).orElse(null);

            if (currentProjectMember == null || (currentProjectMember.getRole() != ProjectRoles.ADMIN && currentProjectMember.getRole() != ProjectRoles.OWNER)) {
                return new ResponseEntity<>(new ErrorResponse("Only project admins can change roles"), HttpStatus.FORBIDDEN);
            }

            User targetUser = userRepository.findByUserName(request.getUserName());
            if (targetUser == null) {
                return new ResponseEntity<>(new ErrorResponse("Target user doesn't exist"), HttpStatus.NOT_FOUND);
            }

            OrganizationMember targetOrgMember = organizationMemberRepository.findByUserAndOrganization(targetUser, project.getOrganization());
            if (targetOrgMember == null) {
                return new ResponseEntity<>(new ErrorResponse("Target user is not a member of this organization"), HttpStatus.BAD_REQUEST);
            }

            ProjectMember targetProjectMember = projectMemberRepository.findByProjectAndOrganizationMember(project, targetOrgMember).orElse(null);
            if (targetProjectMember == null) {
                return new ResponseEntity<>(new ErrorResponse("Target user is not a member of this project"), HttpStatus.BAD_REQUEST);
            }
            if(targetProjectMember.getRole() == ProjectRoles.OWNER && currentProjectMember.getRole() != ProjectRoles.OWNER) {
                return new ResponseEntity<>(new ErrorResponse("Only project owners can change the role of another project owner"), HttpStatus.FORBIDDEN);
            }

            if(targetProjectMember.getRole().equals(request.getNewRole())) {
                return new ResponseEntity<>(new ErrorResponse("Target user already has the specified role"), HttpStatus.BAD_REQUEST);
            }
            targetProjectMember.setRole(request.getNewRole());
            projectMemberRepository.save(targetProjectMember);

            return new ResponseEntity<>(HttpStatus.OK);

        } catch (Exception e) {
            return new ResponseEntity<>(new ErrorResponse("Unable to change project member role"), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{projectId}/project-member-details")
    public ResponseEntity<?> getProjectMemberDetails(
            @PathVariable String projectId) {

        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userName = authentication.getName();
            User user = userService.findByUserName(userName);

            if (user == null) {
                return new ResponseEntity<>(new ErrorResponse("User doesn't exist"), HttpStatus.NOT_FOUND);
            }

            Project project = projectRepository.findByProjectId(projectId);

            if (project == null) {
                return new ResponseEntity<>(new ErrorResponse("Project doesn't exist"), HttpStatus.NOT_FOUND);
            }

            OrganizationMember organizationMember = organizationMemberRepository.findByUserAndOrganization(user, project.getOrganization());

            if (organizationMember == null) {
                return new ResponseEntity<>(new ErrorResponse("User is not a member of the organization"), HttpStatus.FORBIDDEN);
            }

            ProjectMember projectMember = projectMemberRepository.findByProjectAndOrganizationMember(project, organizationMember).orElse(null);

            if (projectMember == null) {
                return new ResponseEntity<>(new ErrorResponse("User is not a member of this project"), HttpStatus.FORBIDDEN);
            }

            ProjectMemberResponse response = projectService.mapToProjectMemberResponse(projectMember);

            return new ResponseEntity<>(response, HttpStatus.OK);

        } catch (Exception e) {
            return new ResponseEntity<>(new ErrorResponse("Unable to fetch project member details"), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}