package com.sdlc.sdlc.entity;

import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "organization_members")
public class OrganizationMember {

    @Id
    private ObjectId id;

    @DBRef
    private User user;

    private String role;
}
