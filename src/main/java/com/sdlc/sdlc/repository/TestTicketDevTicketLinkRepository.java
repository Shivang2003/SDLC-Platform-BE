package com.sdlc.sdlc.repository;

import com.sdlc.sdlc.entity.TestTicketDevTicketLink;
import com.sdlc.sdlc.entity.TestTicketRevision;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface TestTicketDevTicketLinkRepository extends MongoRepository<TestTicketDevTicketLink, ObjectId> {
}
