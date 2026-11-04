package com.sdlc.sdlc.repository;

import com.sdlc.sdlc.entity.ProjectWorklogConfig;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface ProjectWorklogConfigRepository
        extends MongoRepository<ProjectWorklogConfig, String> {

    Optional<ProjectWorklogConfig> findByProjectId(String projectId);
}