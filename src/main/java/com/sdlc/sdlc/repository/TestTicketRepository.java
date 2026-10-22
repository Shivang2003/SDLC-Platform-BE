package com.sdlc.sdlc.repository;

import com.sdlc.sdlc.entity.DevTicket;
import com.sdlc.sdlc.entity.Feature;
import com.sdlc.sdlc.entity.Project;
import com.sdlc.sdlc.entity.TestTicket;
import com.sdlc.sdlc.service.TestTicketService;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface TestTicketRepository extends MongoRepository<TestTicket, ObjectId> {
    TestTicket findByTitleAndFeature(String title, Feature feature);
    TestTicket findByKey(String testTicketId);
    List<TestTicket> findByProjectAndFeature(Project project, Feature feature);
}
