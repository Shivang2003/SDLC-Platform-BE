package com.sdlc.sdlc.controller;


import com.sdlc.sdlc.entity.DevTicket;
import com.sdlc.sdlc.entity.ErrorResponse;
import com.sdlc.sdlc.entity.Project;
import com.sdlc.sdlc.entity.ProjectMember;
import com.sdlc.sdlc.enums.ProjectRoles;
import com.sdlc.sdlc.enums.TicketStatus;
import com.sdlc.sdlc.repository.DevTicketRepository;
import com.sdlc.sdlc.service.DevTicketService;
import com.sdlc.sdlc.service.FeatureService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.ReactiveDatabaseCallback;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;

@Controller
@RequestMapping("/dev-ticket")
@Slf4j
public class DevTicketController {

    @Autowired
    private DevTicketRepository devTicketRepository;

    @Autowired
    FeatureService featureService;

    @Autowired
    DevTicketService devTicketService;

    @PatchMapping("/{ticketKey}/status")
    public ResponseEntity<?> updateDevTicketStatus(@PathVariable String ticketKey, @RequestBody Map<String, TicketStatus> request) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userName = authentication.getName();

            DevTicket devTicket = devTicketRepository.findByKey(ticketKey);
            if (devTicket == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse("Dev ticket not found"));
            }

            Project devTicketProject = devTicket.getProject();

            ProjectMember projectMember = featureService.validateFeatureEditAccess(devTicketProject.getProjectId(), userName, Set.of(ProjectRoles.VIEWER,ProjectRoles.TESTER));

            if (projectMember == null) {
                return ResponseEntity.status(403).body("You cannot create a test ticket for this feature");
            }

            TicketStatus status = request.get("status");

            if (status == null) {
                return ResponseEntity.badRequest()
                        .body(new ErrorResponse("Status is required"));
            }

            DevTicket updatedTicket =
                    devTicketService.updateDevTicketStatus(
                            devTicket,
                            status,
                            projectMember
                    );

            return new ResponseEntity<>(updatedTicket,HttpStatus.OK);

        } catch (Exception e) {
            log.error("Error updating dev ticket status", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponse("Unable to update dev ticket status"));
        }
    }


}
