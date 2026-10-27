package com.sdlc.sdlc.repository;

import com.sdlc.sdlc.entity.ProjectMember;
import com.sdlc.sdlc.entity.User;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.io.ObjectInput;
import java.util.List;

public interface UserRepository extends MongoRepository<User, ObjectId> {
    User findByUserName(String userName);
    User findByEmail(String email);
    List<User> findByUserIdIn(List<String> userIds);
    User findByUserId(String userId);
}
