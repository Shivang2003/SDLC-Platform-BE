package com.sdlc.sdlc.entity;

import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Data
@Document(collection = "organizations")
public class Organization {

    @Id
    private ObjectId id;

    @Indexed(unique = true)
    private String organizationId;

    private String name;
    private String description;
    private List<String> projectId = new ArrayList<>();;
    private List<String> userId = new ArrayList<>();;

}
