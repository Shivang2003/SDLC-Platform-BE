
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
        commentItem.setUserId(commentUser.getUserId());
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

        // USER mentions
        List<Map<String, Object>> users = userRepository
                .findByUserIdIn(project.getUserId())
                .stream()
                .map(user -> Map.<String, Object>of(
                        "userName", user.getUserName(),
                        "id", user.getUserId()
                ))
                .toList();

        // TICKET mentions
        List<Map<String, Object>> tickets = new ArrayList<>();

        tickets.addAll(devTicketRepository
                .findMentionValuesByProjectId(project.getId())
                .stream()
                .map(ticket -> Map.<String, Object>of(
                        "ticketKey", ticket.getKey(),
                        "title", ticket.getTitle()
                ))
                .toList());

        tickets.addAll(testTicketRepository
                .findMentionValuesByProjectId(project.getId())
                .stream()
                .map(ticket -> Map.<String, Object>of(
                        "ticketKey", ticket.getKey(),
                        "title", ticket.getTitle()
                ))
                .toList());

        tickets.addAll(bugTicketRepository
                .findMentionValuesByProjectId(project.getId())
                .stream()
                .map(ticket -> Map.<String, Object>of(
                        "ticketKey", ticket.getKey(),
                        "title", ticket.getTitle()
                ))
                .toList());

        // FEATURE mentions
        List<Map<String, Object>> features = featureRepositroy
                .findByProject(project)
                .stream()
                .map(feature -> Map.<String, Object>of(
                        "featureKey", feature.getFeatureId(),
                        "title", feature.getName()
                ))
                .toList();

        config.getMentions().get("@").setValues(users);
        config.getMentions().get("#").setValues(tickets);
        config.getMentions().get("$").setValues(features);

        return config;
    }
}
