package com.sdlc.sdlc.repository;

import com.sdlc.sdlc.entity.DevTicket;
import com.sdlc.sdlc.entity.DevTicketRevision;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface DevTicketRevisionRepository extends MongoRepository<DevTicketRevision, ObjectId> {

}
