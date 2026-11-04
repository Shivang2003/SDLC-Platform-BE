package com.sdlc.sdlc.service;

import com.sdlc.sdlc.dto.*;
import com.sdlc.sdlc.entity.*;
import com.sdlc.sdlc.enums.ProjectRoles;
import com.sdlc.sdlc.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class WorklogService {

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OrganizationMemberRepository organizationMemberRepository;

    @Autowired
    private ProjectMemberRepository projectMemberRepository;

    @Autowired
    private ProjectWorklogConfigRepository projectWorklogConfigRepository;

    @Autowired
    IdGeneratorService idGeneratorService;

    @Autowired
    WorklogRepository worklogRepository;

    @Autowired
    private MongoTemplate mongoTemplate;

    public ProjectWorklogConfigResponse initializeWorklog(
            String projectId,
            String userName,
            InitializeWorklogRequest request) {

        try {

            Project project = projectRepository.findByProjectId(projectId);

            if (project == null) {
                throw new RuntimeException("Project doesn't exist");
            }

            User user = userRepository.findByUserName(userName);

            if (user == null) {
                throw new RuntimeException("User doesn't exist");
            }

            OrganizationMember organizationMember = organizationMemberRepository.findByUserAndOrganization(user, project.getOrganization());

            if (organizationMember == null) {
                throw new RuntimeException("User is not a member of this project");
            }

            Optional<ProjectMember> projectMember = projectMemberRepository.findByProjectAndOrganizationMember(project, organizationMember);

            if (projectMember.isEmpty()) {
                throw new RuntimeException("User is not a member of this project");
            }

            if (!ProjectRoles.ADMIN.equals(projectMember.get().getRole()) && !ProjectRoles.OWNER.equals(projectMember.get().getRole())) {
                throw new RuntimeException("Only Project Admin or Owner can initialize worklog");
            }

            if (request.getStartTime() == null ||
                    request.getEndTime() == null ||
                    request.getTimeZone() == null ||
                    request.getTimeZone().isBlank()) {
                throw new RuntimeException("Start time, end time and timezone are required");
            }

            if (!request.getStartTime().isBefore(request.getEndTime())) {
                throw new RuntimeException("Start time must be before end time");
            }

            ZoneId zoneId;

            try {
                zoneId = ZoneId.of(request.getTimeZone());
            } catch (Exception e) {
                throw new RuntimeException("Invalid timezone");
            }

            if (projectWorklogConfigRepository.findByProjectId(projectId).isPresent()) {
                throw new RuntimeException("Worklog is already initialized for this project");
            }

            ProjectWorklogConfig config = new ProjectWorklogConfig();
            String workLogId = idGeneratorService.generateSequenceId("worklog", "WL");
            config.setWorkLogId(workLogId);
            config.setProjectId(projectId);
            config.setStartTime(request.getStartTime());
            config.setEndTime(request.getEndTime());
            config.setTimeZone(zoneId.getId());
            config.setEnabled(true);

            config.setCreatedAt(LocalDateTime.now(zoneId));
            config.setUpdatedAt(LocalDateTime.now(zoneId));

            ProjectWorklogConfig savedConfig = projectWorklogConfigRepository.save(config);

            ProjectWorklogConfigResponse response = new ProjectWorklogConfigResponse();

            response.setWorkLogId(savedConfig.getWorkLogId());
            response.setProjectId(savedConfig.getProjectId());
            response.setStartTime(savedConfig.getStartTime());
            response.setEndTime(savedConfig.getEndTime());
            response.setTimeZone(savedConfig.getTimeZone());
            response.setEnabled(savedConfig.isEnabled());
            response.setCreatedAt(savedConfig.getCreatedAt());
            response.setUpdatedAt(savedConfig.getUpdatedAt());

            return response;

        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize worklog");
        }
    }

    public WorklogResponse submitWorklog(String projectId, String userName, SubmitWorklogRequest request) {

        Project project = projectRepository.findByProjectId(projectId);

        if (project == null) {
            throw new RuntimeException("Project doesn't exist");
        }

        User user = userRepository.findByUserName(userName);

        if (user == null) {
            throw new RuntimeException("User doesn't exist");
        }

        OrganizationMember organizationMember = organizationMemberRepository.findByUserAndOrganization(user, project.getOrganization());

        if (organizationMember == null) {
            throw new RuntimeException("User is not a member of this project");
        }

        Optional<ProjectMember> projectMember = projectMemberRepository.findByProjectAndOrganizationMember(project, organizationMember);

        if (projectMember.isEmpty()) {
            throw new RuntimeException("User is not a member of this project");
        }

        ProjectRoles role = projectMember.get().getRole();

        if (ProjectRoles.ADMIN.equals(role)) {
            throw new RuntimeException("Project Admin is not allowed to submit worklogs");
        }

        if (request == null || request.getWorklogText() == null ||request.getWorklogText().isBlank()) {
            throw new RuntimeException("Worklog text is required");
        }

        ProjectWorklogConfig config = projectWorklogConfigRepository.findByProjectId(projectId).orElseThrow(() -> new RuntimeException("Worklog is not initialized for this project"));

        if (!config.isEnabled()) {
            throw new RuntimeException("Worklog is disabled for this project");
        }

        ZoneId zoneId = ZoneId.of(config.getTimeZone());

        ZonedDateTime currentDateTime = ZonedDateTime.now(zoneId);

        LocalTime currentTime = currentDateTime.toLocalTime();
        LocalDate currentDate = currentDateTime.toLocalDate();

        if (currentTime.isBefore(config.getStartTime()) || !currentTime.isBefore(config.getEndTime())) {
            throw new RuntimeException("Worklog submission is allowed only between " + config.getStartTime() + " and " + config.getEndTime());
        }

        if (worklogRepository.existsByProjectIdAndUserIdAndWorkDate(projectId, user.getUserId(), currentDate)) {
            throw new RuntimeException("Worklog already submitted for today");
        }

        Worklog worklog = new Worklog();

        worklog.setWorklogId(idGeneratorService.generateSequenceId("worklog_entry", "WLE"));

        worklog.setProjectId(projectId);
        worklog.setUserId(user.getUserId());
        worklog.setUserName(user.getUserName());
        worklog.setProjectRole(projectMember.get().getRole());
        worklog.setWorkDate(currentDate);
        worklog.setWorklogText(request.getWorklogText());
        worklog.setSubmittedAt(currentDateTime.toLocalDateTime());

        Worklog savedWorklog = worklogRepository.save(worklog);

        WorklogResponse response = new WorklogResponse();
        response.setWorklogId(savedWorklog.getWorklogId());
        response.setProjectId(savedWorklog.getProjectId());
        response.setUserId(savedWorklog.getUserId());
        response.setUserName(savedWorklog.getUserName());
        response.setProjectRole(savedWorklog.getProjectRole());
        response.setWorkDate(savedWorklog.getWorkDate());
        response.setWorklogText(savedWorklog.getWorklogText());
        response.setSubmittedAt(savedWorklog.getSubmittedAt());

        return response;
    }

    public ProjectWorklogReportResponse getProjectRecentWorklogs(String projectId, String userName, LocalDate reportDate) {
        Project project = projectRepository.findByProjectId(projectId);
        if (project == null) {
            throw new RuntimeException("Project doesn't exist");
        }

        User user = userRepository.findByUserName(userName);

        if (user == null) {
            throw new RuntimeException("User doesn't exist");
        }

        OrganizationMember organizationMember = organizationMemberRepository.findByUserAndOrganization(user, project.getOrganization());

        if (organizationMember == null) {
            throw new RuntimeException("User is not a member of this project");
        }

        Optional<ProjectMember> currentProjectMember = projectMemberRepository.findByProjectAndOrganizationMember(project, organizationMember);

        if (currentProjectMember.isEmpty()) {
            throw new RuntimeException("User is not a member of this project");
        }

        ProjectRoles currentRole = currentProjectMember.get().getRole();

        if (!ProjectRoles.ADMIN.equals(currentRole) && !ProjectRoles.OWNER.equals(currentRole)) {
            throw new RuntimeException("Only Project Admin or Owner can view worklogs");
        }

        ProjectWorklogConfig config = projectWorklogConfigRepository.findByProjectId(projectId)
                .orElseThrow(() -> new RuntimeException("Worklog is not initialized for this project"));

        if (!config.isEnabled()) {
            throw new RuntimeException("Worklog is disabled for this project");
        }

        ZoneId zoneId = ZoneId.of(config.getTimeZone());

        if (reportDate == null) {
            reportDate = LocalDate.now(zoneId);
        }

        LocalDateTime start = LocalDateTime.of(reportDate, config.getStartTime());
        LocalDateTime end = LocalDateTime.of(reportDate, config.getEndTime());

        Criteria criteria = Criteria.where("projectId").is(projectId)
                .and("submittedAt").gte(start).lt(end);

        Query query = new Query(criteria);

        List<Worklog> worklogs = mongoTemplate.find(query, Worklog.class);

        List<ProjectMember> projectMembers = projectMemberRepository.findByProject(project);

        Set<String> submittedUserIds = worklogs.stream()
                .map(Worklog::getUserId)
                .collect(Collectors.toSet());

        List<MissingWorklogMemberResponse> missingMembers = new ArrayList<>();

        for (ProjectMember member : projectMembers) {

            if (ProjectRoles.ADMIN.equals(member.getRole())) {
                continue;
            }

            User projectUser = member.getOrganizationMember().getUser();

            if (!submittedUserIds.contains(projectUser.getUserId())) {

                MissingWorklogMemberResponse missing = new MissingWorklogMemberResponse();

                missing.setUserId(projectUser.getUserId());
                missing.setUserName(projectUser.getUserName());
                missing.setProjectRole(member.getRole());

                missingMembers.add(missing);
            }
        }

        List<WorklogResponse> worklogResponses = worklogs.stream()
                .map(worklog -> {
                    WorklogResponse response = new WorklogResponse();

                    response.setWorklogId(worklog.getWorklogId());
                    response.setProjectId(worklog.getProjectId());
                    response.setUserId(worklog.getUserId());
                    response.setUserName(worklog.getUserName());
                    response.setProjectRole(worklog.getProjectRole());
                    response.setWorkDate(worklog.getWorkDate());
                    response.setWorklogText(worklog.getWorklogText());
                    response.setSubmittedAt(worklog.getSubmittedAt());

                    return response;
                })
                .toList();

        ProjectWorklogReportResponse response =
                new ProjectWorklogReportResponse();

        response.setProjectId(project.getProjectId());
        response.setProjectName(project.getName());
        response.setReportDate(reportDate);
        response.setStartTime(config.getStartTime());
        response.setEndTime(config.getEndTime());
        response.setTimeZone(config.getTimeZone());
        response.setWorklogs(worklogResponses);
        response.setMissingMembers(missingMembers);

        return response;
    }

    public ProjectWorklogConfigResponse updateWorklogConfig(String projectId, String userName, UpdateWorklogConfigRequest request) {

        Project project = projectRepository.findByProjectId(projectId);

        if (project == null) {
            throw new RuntimeException("Project doesn't exist");
        }

        User user = userRepository.findByUserName(userName);

        if (user == null) {
            throw new RuntimeException("User doesn't exist");
        }

        OrganizationMember organizationMember =
                organizationMemberRepository.findByUserAndOrganization(
                        user, project.getOrganization());

        if (organizationMember == null) {
            throw new RuntimeException("User is not a member of this project");
        }

        Optional<ProjectMember> projectMember = projectMemberRepository.findByProjectAndOrganizationMember(project, organizationMember);

        if (projectMember.isEmpty()) {
            throw new RuntimeException("User is not a member of this project");
        }

        ProjectRoles role = projectMember.get().getRole();

        if (!ProjectRoles.ADMIN.equals(role) && !ProjectRoles.OWNER.equals(role)) {
            throw new RuntimeException("Only Project Admin or Owner can update worklog configuration");
        }

        if (request == null ||
                request.getStartTime() == null ||
                request.getEndTime() == null ||
                request.getTimeZone() == null ||
                request.getTimeZone().isBlank()) {
            throw new RuntimeException("Start time, end time and timezone are required");
        }

        if (!request.getStartTime().isBefore(request.getEndTime())) {
            throw new RuntimeException("Start time must be before end time");
        }

        ZoneId zoneId;

        try {
            zoneId = ZoneId.of(request.getTimeZone());
        } catch (Exception e) {
            throw new RuntimeException("Invalid timezone");
        }

        ProjectWorklogConfig config =
                projectWorklogConfigRepository.findByProjectId(projectId)
                        .orElseThrow(() -> new RuntimeException("Worklog is not initialized for this project"));

        config.setStartTime(request.getStartTime());
        config.setEndTime(request.getEndTime());
        config.setTimeZone(zoneId.getId());
        config.setUpdatedAt(LocalDateTime.now(zoneId));

        ProjectWorklogConfig savedConfig = projectWorklogConfigRepository.save(config);

        ProjectWorklogConfigResponse response = new ProjectWorklogConfigResponse();

        response.setWorkLogId(savedConfig.getWorkLogId());
        response.setProjectId(savedConfig.getProjectId());
        response.setStartTime(savedConfig.getStartTime());
        response.setEndTime(savedConfig.getEndTime());
        response.setTimeZone(savedConfig.getTimeZone());
        response.setEnabled(savedConfig.isEnabled());
        response.setCreatedAt(savedConfig.getCreatedAt());
        response.setUpdatedAt(savedConfig.getUpdatedAt());

        return response;
    }

    public Map<String, Object> checkWorklogExists(String projectId) {

        Optional<ProjectWorklogConfig> config = projectWorklogConfigRepository.findByProjectId(projectId);
        Map<String, Object> response = new HashMap<>();
        response.put("exists", config.isPresent());

        if (config.isPresent()) {
            response.put("workLogId", config.get().getWorkLogId());
            response.put("enabled", config.get().isEnabled());
        }

        return response;
    }
}