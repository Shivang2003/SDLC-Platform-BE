
package com.sdlc.sdlc.service;

import com.sdlc.sdlc.dto.CreateCommentRequest;
import com.sdlc.sdlc.entity.*;
import com.sdlc.sdlc.enums.ProjectRoles;
import com.sdlc.sdlc.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class CommentService {

    @Autowired
    TicketCommentsRepository ticketCommentsRepository;

    @Autowired
    DevTicketRepository devTicketRepository;

    @Autowired
    FeatureService featureService;

    @Autowired
    MentionConfigRepository mentionConfigRepository;

    @Autowired
    ProjectRepository projectRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    TestTicketRepository testTicketRepository;

    @Autowired
    BugTicketRepository bugTicketRepository;

    @Autowired
    FeatureRepositroy featureRepositroy;

    @Autowired
    UserService userService;

    public TicketComments addComment(
            String ticketKey,
            CreateCommentRequest request,
            String userName) {

        DevTicket devTicket = devTicketRepository.findByKey(ticketKey);

        if (devTicket == null) {
            throw new RuntimeException("DevTicket not found");
        }
        User commentUser = userService.findByUserName(userName);
        // Validate edit access
        featureService.validateFeatureEditAccess(devTicket.getProject().getProjectId(), userName, Set.of(ProjectRoles.VIEWER));

        TicketComments ticketComments =
                ticketCommentsRepository
                        .findByTicketKeyAndTicketType(
                                ticketKey,
                                request.getTicketType()
                        )
                        .orElseGet(() -> {
                            TicketComments tc = new TicketComments();
                            tc.setTicketKey(ticketKey);
                            tc.setTicketType(request.getTicketType());
                            return tc;
                        });

        TicketComments.CommentItem commentItem =
                new TicketComments.CommentItem();

        commentItem.setCommentId("COM-" + UUID.randomUUID());
        TicketComments.UserDetails userDetails =
                new TicketComments.UserDetails();

        userDetails.setUserId(commentUser.getUserId());
        userDetails.setUserName(commentUser.getUserName());
        commentItem.setUser(userDetails);
        commentItem.setTime(LocalDateTime.now());
        commentItem.setComment(request.getComment());

        ticketComments.getComments().add(commentItem);

        return ticketCommentsRepository.save(ticketComments);
    }


    public MentionConfig getMentionConfig(String projectId) {

        Project project = projectRepository.findByProjectId(projectId);

        if (project == null) {
            throw new RuntimeException("Project not found");
        }

        MentionConfig config = mentionConfigRepository
                .findById("default_mentions")
                .orElseThrow(() ->
                        new RuntimeException("Mention configuration not found"));

        String frontendUrl = "http://localhost:5173";
        String organizationId = project.getOrganization().getOrganizationId();

        // USER mentions
        List<Map<String, Object>> users = userRepository
                .findByUserIdIn(project.getUserId())
                .stream()
                .map(user -> Map.<String, Object>of(
                        "userName", user.getUserName(),
                        "id", user.getUserId()
                ))
                .toList();

        // Fetch project features
        List<Feature> features = featureRepositroy.findByProject(project);

        // TICKET mentions
        List<Map<String, Object>> tickets = new ArrayList<>();

        for (Feature feature : features) {

            String featureBaseUrl = frontendUrl
                    + "/organizations/" + organizationId
                    + "/projects/" + project.getProjectId()
                    + "/features/" + feature.getFeatureId()
                    + "?view=projects";

            // Development tickets
            if (feature.getDevTicketId() != null) {
                for (String ticketKey : feature.getDevTicketId()) {

                    DevTicket ticket = devTicketRepository.findByKey(ticketKey);

                    if (ticket != null) {
                        tickets.add(Map.of(
                                "ticketKey", ticket.getKey(),
                                "title", ticket.getTitle(),
                                "baseURL", featureBaseUrl
                                        + "&ticket=" + ticket.getKey()
                                        + "&ticketType=development"
                        ));
                    }
                }
            }

            // Test tickets
            if (feature.getTestTicketId() != null) {
                for (String ticketKey : feature.getTestTicketId()) {

                    TestTicket ticket = testTicketRepository.findByKey(ticketKey);

                    if (ticket != null) {
                        tickets.add(Map.of(
                                "ticketKey", ticket.getKey(),
                                "title", ticket.getTitle(),
                                "baseURL", featureBaseUrl
                                        + "&ticket=" + ticket.getKey()
                                        + "&ticketType=test"
                        ));
                    }
                }
            }

            // Bug tickets
            if (feature.getBugTicketId() != null) {
                for (String ticketKey : feature.getBugTicketId()) {

                    BugTicket ticket = bugTicketRepository.findByKey(ticketKey);

                    if (ticket != null) {
                        tickets.add(Map.of(
                                "ticketKey", ticket.getKey(),
                                "title", ticket.getTitle(),
                                "baseURL", featureBaseUrl
                                        + "&ticket=" + ticket.getKey()
                                        + "&ticketType=bug"
                        ));
                    }
                }
            }
        }

        // FEATURE mentions
        List<Map<String, Object>> featureValues = features.stream()
                .map(feature -> Map.<String, Object>of(
                        "featureKey", feature.getFeatureId(),
                        "title", feature.getName(),
                        "baseURL", frontendUrl
                                + "/organizations/" + organizationId
                                + "/projects/" + project.getProjectId()
                                + "/features/" + feature.getFeatureId()
                                + "?view=projects"
                ))
                .toList();

        config.getMentions().get("@").setValues(users);
        config.getMentions().get("#").setValues(tickets);
        config.getMentions().get("$").setValues(featureValues);

        return config;
    }

}
