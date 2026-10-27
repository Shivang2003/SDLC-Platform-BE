
package com.sdlc.sdlc.service;

import com.sdlc.sdlc.dto.CreateCommentRequest;
import com.sdlc.sdlc.entity.DevTicket;
import com.sdlc.sdlc.entity.MentionConfig;
import com.sdlc.sdlc.entity.TicketComments;
import com.sdlc.sdlc.enums.ProjectRoles;
import com.sdlc.sdlc.repository.DevTicketRepository;
import com.sdlc.sdlc.repository.MentionConfigRepository;
import com.sdlc.sdlc.repository.TicketCommentsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

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

    public TicketComments addComment(
            String ticketKey,
            CreateCommentRequest request,
            String userName) {

        DevTicket devTicket = devTicketRepository.findByKey(ticketKey);

        if (devTicket == null) {
            throw new RuntimeException("DevTicket not found");
        }

        // Validate edit access
        featureService.validateFeatureEditAccess(devTicket.getProject().getProjectId(), userName, Set.of(ProjectRoles.VIEWER));

        TicketComments ticketComments =
                ticketCommentsRepository
                        .findByTicketKeyAndTicketType(
                                request.getTicketKey(),
                                request.getTicketType()
                        )
                        .orElseGet(() -> {
                            TicketComments tc = new TicketComments();
                            tc.setTicketKey(request.getTicketKey());
                            tc.setTicketType(request.getTicketType());
                            return tc;
                        });

        TicketComments.CommentItem commentItem =
                new TicketComments.CommentItem();

        commentItem.setCommentId("COM-" + UUID.randomUUID());
        commentItem.setUserName(userName);
        commentItem.setTime(LocalDateTime.now());
        commentItem.setComment(request.getComment());

        ticketComments.getComments().add(commentItem);

        return ticketCommentsRepository.save(ticketComments);
    }

    public MentionConfig getMentionConfig() {
        return mentionConfigRepository.findById("default_mentions").orElseThrow(() -> new RuntimeException("Mention configuration not found"));
    }
}
