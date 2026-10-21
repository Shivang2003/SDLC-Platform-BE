package com.sdlc.sdlc.repository;

import com.sdlc.sdlc.entity.TestTicket;
import com.sdlc.sdlc.entity.TestTicketDevTicketLink;
import com.sdlc.sdlc.entity.TestTicketRevision;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface TestTicketDevTicketLinkRepository extends MongoRepository<TestTicketDevTicketLink, ObjectId> {
    public List<TestTicketDevTicketLink>  findByDevTicketId(String devTicketId);
}
