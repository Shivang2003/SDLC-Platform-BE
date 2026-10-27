package com.sdlc.sdlc.entity;

import com.sdlc.sdlc.enums.FeatureStatus;
import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
@Document(collection = "features")
public class Feature {

    @Id
    private ObjectId id;

    @Indexed(unique = true)
    private String key;// FEAT-001

    private String name;
    private String description;

    @DBRef
    private Project project;

    @DBRef
    private ProjectMember createdBy;

    @Indexed(unique = true)
    private String featureId;

    private FeatureStatus status;

    private ObjectId targetVersionId;
    private List<String> devTicketId = new ArrayList<>();
    private List<String> testTicketId = new ArrayList<>();
    private List<String> bugTicketId = new ArrayList<>();
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
