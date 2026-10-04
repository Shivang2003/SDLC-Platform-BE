package com.sdlc.sdlc.entity;

import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@Document(collection = "projects")
public class Project {
    @Id
    private ObjectId id;

    private String name;
    private String description;
    private String status;

    @DBRef
    private List<ProjectMember> members;
}
