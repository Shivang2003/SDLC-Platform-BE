package com.sdlc.sdlc.repository;

import com.sdlc.sdlc.entity.*;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ProjectMemberRepository extends MongoRepository<ProjectMember, ObjectId> {
    ProjectMember findByOrganizationMemberAndProject(OrganizationMember organizationMember, Project project);
}
