package com.sdlc.sdlc.repository;

import com.sdlc.sdlc.entity.DevTicketStatusHistory;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface DevTicketStatusHistoryRepository extends MongoRepository<DevTicketStatusHistory, ObjectId> {
}
