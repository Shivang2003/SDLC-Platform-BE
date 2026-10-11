package com.sdlc.sdlc.repository;

import com.sdlc.sdlc.entity.*;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface FeatureRepositroy extends MongoRepository<Feature, ObjectId> {
    Feature findByNameAndProject(String name, Project project);
}
