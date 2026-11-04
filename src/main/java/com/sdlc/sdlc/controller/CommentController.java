package com.sdlc.sdlc.controller;

import com.sdlc.sdlc.dto.CreateCommentRequest;
import com.sdlc.sdlc.entity.ErrorResponse;
import com.sdlc.sdlc.entity.TicketComments;
import com.sdlc.sdlc.repository.TicketCommentsRepository;
import com.sdlc.sdlc.service.CommentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.NoSuchElementException;
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

        }  catch (AccessDeniedException e) {

            return new ResponseEntity<>(new ErrorResponse(e.getMessage()), HttpStatus.FORBIDDEN);

        } catch (NoSuchElementException e) {

            return new ResponseEntity<>(new ErrorResponse(e.getMessage()), HttpStatus.NOT_FOUND);

        } catch (IllegalArgumentException e) {

            return new ResponseEntity<>(new ErrorResponse(e.getMessage()), HttpStatus.BAD_REQUEST);

        } catch (IllegalStateException e) {

            return new ResponseEntity<>(new ErrorResponse(e.getMessage()), HttpStatus.CONFLICT);

        } catch (Exception e) {

            log.error("Error adding comment to ticket: {}", ticketKey, e);

            return new ResponseEntity<>(new ErrorResponse("Unable to add comment"), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{ticketKey}")
    public ResponseEntity<?> getComments(@PathVariable String ticketKey){
        try{
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userName = authentication.getName();

            Optional<TicketComments> ticketComments = ticketCommentsRepository.findByTicketKey(ticketKey);
            if(ticketComments.isEmpty()){
                return new ResponseEntity<>(new ErrorResponse("No one commented on this ticket"),HttpStatus.NOT_FOUND);
            }
            return new ResponseEntity<>(ticketComments, HttpStatus.OK);
        }
        catch (AccessDeniedException e) {

            return new ResponseEntity<>(new ErrorResponse(e.getMessage()), HttpStatus.FORBIDDEN);

        } catch (NoSuchElementException e) {

            return new ResponseEntity<>(new ErrorResponse(e.getMessage()), HttpStatus.NOT_FOUND);

        } catch (Exception e) {

            log.error("Error fetching comments for ticket: {}", ticketKey, e);

            return new ResponseEntity<>(new ErrorResponse("Unable to fetch comments"), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
