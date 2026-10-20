package com.sdlc.sdlc.repository;

import com.sdlc.sdlc.entity.Feature;
import com.sdlc.sdlc.entity.TestTicket;
import com.sdlc.sdlc.entity.TestTicketRevision;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface TestTicketRevisionRepository extends MongoRepository<TestTicketRevision, ObjectId> {
}
