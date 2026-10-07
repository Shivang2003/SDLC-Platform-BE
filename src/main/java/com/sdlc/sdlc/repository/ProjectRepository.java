package com.sdlc.sdlc.repository;

import com.sdlc.sdlc.entity.Organization;
import com.sdlc.sdlc.entity.Project;
import com.sdlc.sdlc.entity.User;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ProjectRepository extends MongoRepository<Project, ObjectId> {
    Project findByNameAndOrganization(String name, Organization organization);
}
