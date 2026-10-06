package com.sdlc.sdlc.repository;

import com.sdlc.sdlc.entity.Organization;
import com.sdlc.sdlc.entity.User;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface OrganizationRepository extends MongoRepository<Organization, ObjectId> {
    Organization findByName(String organizationName);
}
