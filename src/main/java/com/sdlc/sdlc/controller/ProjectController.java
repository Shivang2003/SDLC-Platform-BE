package com.sdlc.sdlc.controller;

import com.sdlc.sdlc.dto.AddProjectMemberRequest;
import com.sdlc.sdlc.dto.DevTicketBoardResponse;
import com.sdlc.sdlc.dto.TestTicketBoardResponse;
import com.sdlc.sdlc.entity.*;
import com.sdlc.sdlc.enums.ProjectRoles;
import com.sdlc.sdlc.enums.TestExecutionStatus;
import com.sdlc.sdlc.enums.TicketStatus;
import com.sdlc.sdlc.repository.*;
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
import com.sdlc.sdlc.dto.FeatureSummaryResponse;

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

    @PostMapping("/{projectId}/create-feature")
    public ResponseEntity<?> createFeature(@PathVariable String projectId, @RequestBody Feature feature) {
        try{
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userName = authentication.getName();
            ProjectMember projectMember = featureService.validateFeatureEditAccess(projectId, userName, Set.of(ProjectRoles.VIEWER));
            if(projectMember == null) {
                return new ResponseEntity<>(new ErrorResponse("User is not a member of the project"), HttpStatus.FORBIDDEN);
            }
            Project projectInDb = projectService.findProjectById(projectId);
            if (projectInDb == null) {
                return new ResponseEntity<>(new ErrorResponse("Project doesn't exist"), HttpStatus.NOT_FOUND);
            }
            Feature checkFeature = featureService.checkIfFeatureNameExists(feature.getName(), projectInDb);
            if(checkFeature != null){
                return new ResponseEntity<>(new ErrorResponse("Feature already exists"), HttpStatus.CONFLICT);
            }
            featureService.createNewFeature(feature, projectInDb, projectMember);

            return new ResponseEntity<>(feature, HttpStatus.OK);
        }
        catch (Exception e) {
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

            if(response.isEmpty()){
                return new ResponseEntity<>(new ErrorResponse("no features available"),HttpStatus.FORBIDDEN);
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

            if (currentProjectMember == null || !"ADMIN".equals(currentProjectMember.getRole())) {
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
            String role = userDetails.getRole();

            if (!List.of("ADMIN", "MEMBER", "VIEWER").contains(role)) {
                return new ResponseEntity<>(new ErrorResponse("Invalid project role"), HttpStatus.BAD_REQUEST);
            }

            ProjectMember projectMember = projectService.addMemberToProject(newOrgMember, project, role);
            return new ResponseEntity<>(projectMember, HttpStatus.CREATED);

        }
        catch (IllegalStateException e) {
            return new ResponseEntity<>(new ErrorResponse(e.getMessage()), HttpStatus.CONFLICT);
        }
        catch (Exception e) {
            return new ResponseEntity<>(new ErrorResponse("Unable to add project member"), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{projectId}/features/{featureId}/dev-tickets")
    public ResponseEntity<?> getAllDevTickets(@PathVariable String projectId,@PathVariable String featureId){
        try{
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String currentUserName = authentication.getName();

            Project project = projectRepository.findByProjectId(projectId);
            if (project == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Project not found");
            }

            Feature feature = featureService.checkIfFeatureIdExists(featureId);
            if(feature == null){
                return ResponseEntity.status(404).body("Feature doesn't exist");
            }

            List<DevTicket> devTickets = devTicketRepository.findByProjectAndFeature(project,feature);

            List<DevTicketBoardResponse.DevTicketItem> ticketItems = devTickets.stream()
                            .map(ticket -> new DevTicketBoardResponse.DevTicketItem(
                                            ticket.getTitle(),
                                            ticket.getPriority(),
                                            ticket.getKey(),
                                            ticket.getStatus(),
                                            ticket.getUpdatedAt())).toList();

            DevTicketBoardResponse response = new DevTicketBoardResponse(List.of(TicketStatus.values()), ticketItems);

            return new ResponseEntity<>(response, HttpStatus.OK);

        }
        catch (Exception e){
            return new ResponseEntity<>(new ErrorResponse("Unable to add project member"), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{projectId}/features/{featureId}/test-tickets")
    public ResponseEntity<?> getAllTestTickets(@PathVariable String projectId,@PathVariable String featureId){
       try{
           Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
           String currentUserName = authentication.getName();

           Project project = projectRepository.findByProjectId(projectId);
           if (project == null) {
               return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Project not found");
           }

           Feature feature = featureService.checkIfFeatureIdExists(featureId);
           if(feature == null){
               return ResponseEntity.status(404).body("Feature doesn't exist");
           }

           List<TestTicket> testTickets = testTicketRepository.findByProjectAndFeature(project,feature);

           List<TestTicketBoardResponse.TestTicketItem> ticketItems = testTickets.stream()
                   .map(ticket -> new TestTicketBoardResponse.TestTicketItem(
                           ticket.getTitle(),
                           ticket.getPriority(),
                           ticket.getKey(),
                           ticket.getExecutionStatus(),
                           ticket.getUpdatedAt())).toList();

           TestTicketBoardResponse response = new TestTicketBoardResponse(List.of(TestExecutionStatus.values()), ticketItems);

           return new ResponseEntity<>(response, HttpStatus.OK);

       }
       catch (Exception e){
           return new ResponseEntity<>(new ErrorResponse("Unable to add project member"), HttpStatus.INTERNAL_SERVER_ERROR);
       }
    }

    @GetMapping("/{projectId}/project-members")
    public ResponseEntity<?> getAllTestTickets(@PathVariable String projectId){
        try{
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
        }
        catch (Exception e){
            return new ResponseEntity<>(new ErrorResponse("Unable to add project member"), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
