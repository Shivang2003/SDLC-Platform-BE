package com.sdlc.sdlc.entity;

import com.sdlc.sdlc.enums.VersionStatus;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "project_versions")
public class ProjectVersion {

    @Id
    private ObjectId id;

    private String version;          // 1.0.0
    private String name;             // Initial Release

    @DBRef
    private Project project;

    private VersionStatus status;

    private String description;

    private LocalDateTime createdAt;
    private LocalDateTime releasedAt;
}
