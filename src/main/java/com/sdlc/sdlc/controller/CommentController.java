package com.sdlc.sdlc.controller;

import com.sdlc.sdlc.dto.CreateCommentRequest;
import com.sdlc.sdlc.entity.TicketComments;
import com.sdlc.sdlc.repository.TicketCommentsRepository;
import com.sdlc.sdlc.service.CommentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;


@Controller
@RequestMapping("/comment")
@Slf4j
public class CommentController {

    @Autowired
    CommentService commentService;

    @Autowired
    TicketCommentsRepository ticketCommentsRepository;

    @PostMapping("/{ticketKey}")
    public ResponseEntity<?> addComment(@PathVariable String ticketKey, @RequestBody CreateCommentRequest request) {

        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userName = authentication.getName();

            TicketComments response = commentService.addComment(ticketKey, request, userName);
            return new ResponseEntity<>(response, HttpStatus.CREATED);

        } catch (RuntimeException e) {
            return new ResponseEntity<>(
                    e.getMessage(),
                    HttpStatus.BAD_REQUEST
            );
        }
    }

    @GetMapping("/{ticketKey}")
    public ResponseEntity<?> getComments(@PathVariable String ticketKey){
        try{
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userName = authentication.getName();

            Optional<TicketComments> ticketComments = ticketCommentsRepository.findByTicketKey(ticketKey);
            if(ticketComments == null){
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
            return new ResponseEntity<>(ticketComments, HttpStatus.CREATED);
        }
        catch (RuntimeException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
}
