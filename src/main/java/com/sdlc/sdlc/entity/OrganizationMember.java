package com.sdlc.sdlc.entity;

import com.sdlc.sdlc.enums.OrganizationRoles;
import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@Document(collection = "organization_members")
public class OrganizationMember {

    @Id
    private ObjectId id;

    @DBRef
    private User user;
    @DBRef
    private Organization organization;
    private OrganizationRoles role;
    private String userId;
}
