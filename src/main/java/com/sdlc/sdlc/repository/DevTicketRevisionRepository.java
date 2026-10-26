package com.sdlc.sdlc.repository;

import com.sdlc.sdlc.entity.DevTicket;
import com.sdlc.sdlc.entity.DevTicketRevision;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface DevTicketRevisionRepository extends MongoRepository<DevTicketRevision, ObjectId> {
    List<DevTicketRevision> findByTicketKeyOrderByRevisionNumberDesc(String ticketKey);
    List<DevTicketRevision> findByTicketKey(String ticketKey);
}
