package com.sdlc.sdlc.dto;

import com.sdlc.sdlc.enums.Priority;
import com.sdlc.sdlc.enums.TicketStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.bson.types.ObjectId;

@Data
@AllArgsConstructor
public class DevTicketDetailsResponse {

    private String key;
    private String title;
    private String description;
    private TicketStatus status;
    private Priority priority;
    private Integer currentRevision;
    private ObjectId targetVersionId;
    private String projectId;
    private String featureId;
}