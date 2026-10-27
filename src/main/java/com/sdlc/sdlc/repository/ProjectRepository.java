package com.sdlc.sdlc.repository;

import com.sdlc.sdlc.entity.Organization;
import com.sdlc.sdlc.entity.OrganizationMember;
import com.sdlc.sdlc.entity.Project;
import com.sdlc.sdlc.entity.User;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface ProjectRepository extends MongoRepository<Project, ObjectId> {
    Project findByNameAndOrganization(String name, Organization organization);
    Project findByProjectId(String projectId);
    List<Project> findByOrganization(Organization org);
    @Query(value = "{ 'projectId': ?0 }",
            fields = "{ 'featureId': 1, 'userId': 1, '_id': 0 }")
    ProjectFeatureUserIds findFeatureAndUserIdsByProjectId(String projectId);
}
