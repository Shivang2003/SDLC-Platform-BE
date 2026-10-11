package com.sdlc.sdlc.entity;

import com.sdlc.sdlc.enums.FeatureStatus;
import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Document(collection = "features")
public class Feature {

    @Id
    private ObjectId id;

    private String key;              // FEAT-001
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

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
