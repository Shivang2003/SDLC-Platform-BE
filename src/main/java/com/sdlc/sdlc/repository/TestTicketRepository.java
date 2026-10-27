package com.sdlc.sdlc.repository;

import com.sdlc.sdlc.entity.DevTicket;
import com.sdlc.sdlc.entity.Feature;
import com.sdlc.sdlc.entity.Project;
import com.sdlc.sdlc.entity.TestTicket;
import com.sdlc.sdlc.service.TestTicketService;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface TestTicketRepository extends MongoRepository<TestTicket, ObjectId> {
    TestTicket findByTitleAndFeature(String title, Feature feature);
    TestTicket findByKey(String testTicketId);
    List<TestTicket> findByProjectAndFeature(Project project, Feature feature);
    @Query(value = "{ 'key': ?0 }", fields = "{ 'title': 1, '_id': 0 }")
    TicketTitleProjection findTitleByKey(String ticketKey);
    @Query(value = "{ 'project.$id': ?0 }",
            fields = "{ 'key': 1, 'title': 1, '_id': 0 }")
    List<TicketTitleProjection> findMentionValuesByProjectId(ObjectId projectId);
}
