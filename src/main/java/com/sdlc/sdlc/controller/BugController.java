package com.sdlc.sdlc.controller;

import com.sdlc.sdlc.dto.BugTicketDetailsResponse;
import com.sdlc.sdlc.entity.*;
import com.sdlc.sdlc.repository.BugTicketRepository;
import com.sdlc.sdlc.repository.FeatureRepositroy;
import com.sdlc.sdlc.repository.ProjectRepository;
import com.sdlc.sdlc.repository.UserRepository;
import com.sdlc.sdlc.service.FeatureService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.NoSuchElementException;
import java.util.Set;

@Controller
@RequestMapping("/bug")
@Slf4j
public class BugController {

    @Autowired
    BugTicketRepository bugTicketRepository;

    @Autowired
    FeatureService featureService;

    @Autowired
    ProjectRepository projectRepository;

    @Autowired
    FeatureRepositroy featureRepositroy;

    @Autowired
    UserRepository userRepository;

    @GetMapping("/{ticketKey}/details")
    public ResponseEntity<?> getBugTicketDetails(@PathVariable String ticketKey) {

        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userName = authentication.getName();

            User userInDb = userRepository.findByUserName(userName);
            if (userInDb == null) {
                return new ResponseEntity<>(new ErrorResponse("User not found"), HttpStatus.NOT_FOUND);
            }

            BugTicket bugTicket = bugTicketRepository.findByKey(ticketKey);

            if (bugTicket == null) {
                return new ResponseEntity<>(
                        new ErrorResponse("Bug ticket not found"),
                        HttpStatus.NOT_FOUND
                );
            }

            featureService.validateFeatureEditAccess(bugTicket.getProject().getProjectId(), userName, Set.of());

            Project projectInDb = projectRepository.findByProjectId(bugTicket.getProject().getProjectId());
            if (projectInDb == null) {
                throw new NoSuchElementException("Referenced project not found");
            }

            if(!projectInDb.getUserId().contains(userInDb.getUserId())){
                throw new AccessDeniedException("User does not have access to this project");
            }

            Feature featureInDb = featureRepositroy.findByFeatureId(bugTicket.getFeature().getFeatureId());
            if (featureInDb == null) {
                throw new NoSuchElementException("Referenced feature not found" );
            }

            BugTicketDetailsResponse response =
                    new BugTicketDetailsResponse(
                            bugTicket.getKey(),
                            bugTicket.getTitle(),
                            bugTicket.getDescription(),
                            bugTicket.getStepsToReproduce(),
                            bugTicket.getExpectedResult(),
                            bugTicket.getActualResult(),
                            bugTicket.getStatus(),
                            bugTicket.getPriority(),
                            bugTicket.getSeverity(),
                            bugTicket.getCurrentRevision(),
                            bugTicket.getTargetVersionId(),
                            projectInDb.getName(),
                            featureInDb.getName(),
                            bugTicket.getTestCase() != null
                                    ? bugTicket.getTestCase().getKey()
                                    : null
                    );

            return new ResponseEntity<>(response, HttpStatus.OK);

        } catch (AccessDeniedException e) {

            return new ResponseEntity<>(
                    new ErrorResponse(e.getMessage()),
                    HttpStatus.FORBIDDEN
            );

        } catch (NoSuchElementException e) {
            return new ResponseEntity<>(new ErrorResponse(e.getMessage()), HttpStatus.NOT_FOUND);
        } catch (IllegalStateException e) {
            return new ResponseEntity<>(new ErrorResponse(e.getMessage()), HttpStatus.CONFLICT);
        }catch (Exception e) {
            log.error("Error fetching BugTicket details", e);

            return new ResponseEntity<>(
                    new ErrorResponse("Unable to fetch BugTicket details"),
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }
}
