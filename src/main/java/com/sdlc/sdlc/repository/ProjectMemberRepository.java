package com.sdlc.sdlc.repository;

import com.sdlc.sdlc.entity.*;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface ProjectMemberRepository extends MongoRepository<ProjectMember, ObjectId> {
    ProjectMember findByOrganizationMemberAndProject(OrganizationMember organizationMember, Project project);
    Optional<ProjectMember> findByProject_IdAndOrganizationMember(Project project, OrganizationMember organizationMember);
    List<ProjectMember> findByProject_Id(ObjectId projectId);
    List<ProjectMember> findByOrganizationMemberAndProject_Organization(OrganizationMember organizationMember, Organization organization);
    List<ProjectMember> findByOrganizationMember(OrganizationMember organizationMember);
    Optional<ProjectMember> findByProjectAndOrganizationMember(Project project, OrganizationMember organizationMember);
}
