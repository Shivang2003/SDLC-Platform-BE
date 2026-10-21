package com.sdlc.sdlc.dto;

import com.sdlc.sdlc.enums.TicketStatus;
import com.sdlc.sdlc.enums.Priority;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
public class DevTicketBoardResponse {

    private List<TicketStatus> status;
    private List<DevTicketItem> devTickets;

    @Data
    @AllArgsConstructor
    public static class DevTicketItem {

        private String name;
        private Priority priority;
        private String key;
        private TicketStatus status;
        private LocalDateTime updatedAt;
    }
}