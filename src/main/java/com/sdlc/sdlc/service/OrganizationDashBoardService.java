package com.sdlc.sdlc.service;

import com.sdlc.sdlc.dto.OrganizationDashboardSummary;
import com.sdlc.sdlc.entity.*;
import com.sdlc.sdlc.enums.BugStatus;
import com.sdlc.sdlc.enums.Severity;
import com.sdlc.sdlc.enums.TicketStatus;
import com.sdlc.sdlc.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.sdlc.sdlc.repository.FeatureRepositroy;
import com.sdlc.sdlc.enums.TestExecutionStatus;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class OrganizationDashBoardService {

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private FeatureRepositroy featureRepository;

    @Autowired
    private DevTicketRepository devTicketRepository;

    @Autowired
    private TestTicketRepository testTicketRepository;

    @Autowired
    private BugTicketRepository bugTicketRepository;

    public OrganizationDashboardSummary getSummary(String organizationId) {

        Organization organization =
                organizationRepository.findByOrganizationId(organizationId);

        if (organization == null) {
            throw new RuntimeException("Organization not found");
        }

        List<Project> projects =
                projectRepository.findByOrganization(organization);

        long totalFeatures = 0;
        long totalDevTickets = 0;
        long completedDevTickets = 0;
        long totalTestTickets = 0;
        long passedTests = 0;
        long failedTests = 0;
        long totalBugTickets = 0;
        long openBugs = 0;

        for (Project project : projects) {

            List<Feature> features =
                    featureRepository.findByProject(project);

            totalFeatures += features.size();

            for (Feature feature : features) {

                List<DevTicket> devTickets =
                        devTicketRepository.findByProjectAndFeature(
                                project, feature);

                totalDevTickets += devTickets.size();

                completedDevTickets += devTickets.stream()
                        .filter(ticket ->
                                ticket.getStatus() == TicketStatus.DONE)
                        .count();

                List<TestTicket> testTickets =
                        testTicketRepository.findByProjectAndFeature(
                                project, feature);

                totalTestTickets += testTickets.size();

                passedTests += testTickets.stream()
                        .filter(ticket ->
                                ticket.getExecutionStatus()
                                        == TestExecutionStatus.PASS)
                        .count();

                failedTests += testTickets.stream()
                        .filter(ticket ->
                                ticket.getExecutionStatus()
                                        == TestExecutionStatus.FAIL)
                        .count();

                List<BugTicket> bugTickets =
                        bugTicketRepository.findByProjectAndFeature(
                                project, feature);

                totalBugTickets += bugTickets.size();

                openBugs += bugTickets.stream()
                        .filter(ticket ->
                                ticket.getStatus() != BugStatus.FIXED
                                        && ticket.getStatus() != BugStatus.CLOSED)
                        .count();
            }
        }

        OrganizationDashboardSummary summary =
                new OrganizationDashboardSummary();

        summary.setTotalProjects(projects.size());
        summary.setTotalFeatures(totalFeatures);
        summary.setTotalDevTickets(totalDevTickets);
        summary.setCompletedDevTickets(completedDevTickets);
        summary.setTotalTestTickets(totalTestTickets);
        summary.setPassedTests(passedTests);
        summary.setFailedTests(failedTests);
        summary.setTotalBugTickets(totalBugTickets);
        summary.setOpenBugs(openBugs);

        return summary;
    }

    public Map<String, Long> getDevTicketStats(String organizationId) {

        Organization organization =
                organizationRepository.findByOrganizationId(organizationId);

        if (organization == null) {
            throw new RuntimeException("Organization not found");
        }

        List<Project> projects =
                projectRepository.findByOrganization(organization);

        List<DevTicket> devTickets =
                devTicketRepository.findByProjectIn(projects);

        Map<String, Long> stats = new LinkedHashMap<>();

        for (TicketStatus status : TicketStatus.values()) {
            stats.put(
                    status.name(),
                    devTickets.stream()
                            .filter(ticket -> ticket.getStatus() == status)
                            .count()
            );
        }

        return stats;
    }

    public Map<String, Long> getBugStats(String organizationId) {

        Organization organization =
                organizationRepository.findByOrganizationId(organizationId);

        if (organization == null) {
            throw new RuntimeException("Organization not found");
        }

        List<Project> projects =
                projectRepository.findByOrganization(organization);

        Map<String, Long> stats = new LinkedHashMap<>();

        for (Severity severity : Severity.values()) {
            stats.put(severity.name(), 0L);
        }

        if (projects.isEmpty()) {
            return stats;
        }

        List<BugTicket> bugTickets =
                bugTicketRepository.findByProjectIn(projects);

        for (BugTicket bugTicket : bugTickets) {
            Severity severity = bugTicket.getSeverity();

            if (severity != null) {
                stats.merge(severity.name(), 1L, Long::sum);
            }
        }

        return stats;
    }
}
