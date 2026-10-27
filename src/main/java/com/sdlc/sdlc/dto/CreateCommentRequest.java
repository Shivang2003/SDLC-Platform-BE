package com.sdlc.sdlc.dto;

import com.sdlc.sdlc.enums.TicketType;
import lombok.Data;

import java.util.Map;

@Data
public class CreateCommentRequest {

    private TicketType ticketType;

    private Map<String, Object> comment;
}
