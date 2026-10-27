package com.sdlc.sdlc.entity;

import com.sdlc.sdlc.enums.TicketType;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
@Document(collection = "comments")
public class TicketComments {

    @Id
    private String id;

    private String ticketKey;

    private TicketType ticketType;

    private List<CommentItem> comments = new ArrayList<>();

    @Data
    public static class CommentItem {
        private String commentId;
        private String userName;
        private LocalDateTime time;
        private Map<String, Object> comment;
    }
}
