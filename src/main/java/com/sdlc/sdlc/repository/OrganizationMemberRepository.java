package com.sdlc.sdlc.repository;

import com.sdlc.sdlc.entity.Organization;
import com.sdlc.sdlc.entity.OrganizationMember;
import com.sdlc.sdlc.entity.User;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface OrganizationMemberRepository extends MongoRepository<OrganizationMember, ObjectId> {
    OrganizationMember findByUserAndOrganization(User user, Organization organization);
    List<OrganizationMember> findByUser(User user);
}
