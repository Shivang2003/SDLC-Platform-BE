package com.sdlc.sdlc.repository;

import com.sdlc.sdlc.entity.TestTicketStatusHistory;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface TestTicketStatusHistoryRepository extends MongoRepository<TestTicketStatusHistory, ObjectId> {
}
