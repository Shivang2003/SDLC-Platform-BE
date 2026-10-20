package com.sdlc.sdlc.repository;

import com.sdlc.sdlc.entity.DevTicket;
import com.sdlc.sdlc.entity.Feature;
import com.sdlc.sdlc.entity.TestTicket;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface TestTicketRepository extends MongoRepository<TestTicket, ObjectId> {
    TestTicket findByTitleAndFeature(String title, Feature feature);
}
