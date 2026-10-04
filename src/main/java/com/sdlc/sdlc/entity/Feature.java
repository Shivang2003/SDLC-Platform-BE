package com.sdlc.sdlc.entity;

import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "features")
public class Feature {

    @Id
    private ObjectId id;

    private String featureId;
    private String title;
    private String description;
    private String status;
    private String priority;

    @DBRef
    private Project project;

    @DBRef
    private User createdBy;
}
