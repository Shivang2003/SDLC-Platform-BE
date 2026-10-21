package com.sdlc.sdlc.controller;


import com.sdlc.sdlc.entity.DevTicket;
import com.sdlc.sdlc.entity.ErrorResponse;
import com.sdlc.sdlc.enums.TicketStatus;
import com.sdlc.sdlc.repository.DevTicketRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@Controller
@RequestMapping("/dev-ticket")
@Slf4j
public class DevTicketController {

    @Autowired
    private DevTicketRepository devTicketRepository;

    @PatchMapping("/{ticketKey}/status")
    public ResponseEntity<?> updateDevTicketStatus(@PathVariable String ticketKey, @RequestBody Map<String, TicketStatus> request) {
        try {
            DevTicket devTicket = devTicketRepository.findByKey(ticketKey);
            if (devTicket == null) {return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse("Dev ticket not found"));
            }
            TicketStatus status = request.get("status");
            if (status == null) {return ResponseEntity.badRequest().body(new ErrorResponse("Status is required"));
            }
            devTicket.setStatus(status);
            devTicket.setUpdatedAt(LocalDateTime.now());
            DevTicket updatedTicket = devTicketRepository.save(devTicket);

            return ResponseEntity.ok(updatedTicket);

        } catch (Exception e) {
            log.error("Error updating dev ticket status", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponse("Unable to update dev ticket status"));
        }
    }
}
