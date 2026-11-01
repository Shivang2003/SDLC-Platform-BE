package com.sdlc.sdlc.controller;

import com.sdlc.sdlc.entity.ErrorResponse;
import com.sdlc.sdlc.service.CommentService;
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

@Controller
@RequestMapping("/mention")
@Slf4j
public class MentionController {

    @Autowired
    CommentService commentService;

    @GetMapping("/{projectId}")
    public ResponseEntity<?> getMentionConfig(@PathVariable String projectId) {

        try {
            Authentication authentication =
                    SecurityContextHolder.getContext().getAuthentication();

            String userName = authentication.getName();

            return new ResponseEntity<>(
                    commentService.getMentionConfig(projectId),
                    HttpStatus.OK
            );

        } catch (AccessDeniedException e) {
            return new ResponseEntity<>(
                    new ErrorResponse(e.getMessage()),
                    HttpStatus.FORBIDDEN
            );

        } catch (NoSuchElementException e) {
            return new ResponseEntity<>(
                    new ErrorResponse(e.getMessage()),
                    HttpStatus.NOT_FOUND
            );

        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(
                    new ErrorResponse(e.getMessage()),
                    HttpStatus.BAD_REQUEST
            );

        } catch (Exception e) {
            log.error("Error fetching mention config for project {}: {}",
                    projectId, e.getMessage(), e);

            return new ResponseEntity<>(
                    new ErrorResponse("Something went wrong while fetching mention config"),
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }
}