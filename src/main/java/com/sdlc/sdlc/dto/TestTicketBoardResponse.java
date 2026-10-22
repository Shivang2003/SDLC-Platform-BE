package com.sdlc.sdlc.dto;


import com.sdlc.sdlc.enums.Priority;
import com.sdlc.sdlc.enums.TestExecutionStatus;
import com.sdlc.sdlc.enums.TicketStatus;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
public class TestTicketBoardResponse {

    private List<TestExecutionStatus> status;
    private List<TestTicketBoardResponse.TestTicketItem> devTickets;

    @Data
    @AllArgsConstructor
    public static class TestTicketItem {

        private String name;
        private Priority priority;
        private String key;
        private TestExecutionStatus status;
        private LocalDateTime updatedAt;
    }
}
