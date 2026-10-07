package com.sdlc.sdlc.entity;

import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "project_members")
public class ProjectMember {

    @Id
    private ObjectId id;

    @DBRef
    private OrganizationMember organizationMember;

    @DBRef
    private Project project;

    private String role;
}
