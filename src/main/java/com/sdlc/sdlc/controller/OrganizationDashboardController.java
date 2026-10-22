package com.sdlc.sdlc.controller;


import com.sdlc.sdlc.entity.ErrorResponse;
import com.sdlc.sdlc.service.OrganizationDashBoardService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/organization/{organizationId}/dashboard")
@Slf4j
public class OrganizationDashboardController {

    @Autowired
    OrganizationDashBoardService organizationDashboardService;

    @GetMapping("/summary")
    public ResponseEntity<?> getOrganizationDashboardSummary(
            @PathVariable String organizationId) {
        try {
            return new ResponseEntity<>(
                    organizationDashboardService.getSummary(organizationId),
                    HttpStatus.OK
            );
        } catch (RuntimeException e) {
            log.error("Error fetching organization dashboard summary", e);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse(
                            "Error fetching organization dashboard summary"));
        }
    }
    @GetMapping("/dev-ticket-stats")
    public ResponseEntity<?> getDevTicketStats(
            @PathVariable String organizationId) {
        try {
            return new ResponseEntity<>(
                    organizationDashboardService.getDevTicketStats(
                            organizationId),
                    HttpStatus.OK
            );
        } catch (RuntimeException e) {
            log.error("Error fetching DevTicket stats", e);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Error fetching DevTicket stats"));
        }
    }
    @GetMapping("/bug-stats")
    public ResponseEntity<?> getBugStats(
            @PathVariable String organizationId) {
        try {
            return new ResponseEntity<>(
                    organizationDashboardService.getBugStats(organizationId),
                    HttpStatus.OK
            );
        } catch (RuntimeException e) {
            log.error("Error fetching BugTicket stats", e);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Error fetching BugTicket stats"));
        }
    }
}
