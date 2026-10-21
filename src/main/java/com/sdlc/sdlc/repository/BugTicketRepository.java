package com.sdlc.sdlc.repository;

import com.sdlc.sdlc.entity.BugTicket;
import com.sdlc.sdlc.entity.TestTicket;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface BugTicketRepository  extends MongoRepository<BugTicket, ObjectId> {
    BugTicket findByKey(String key);
    List<BugTicket> findByTestCase(TestTicket testCase);
    List<BugTicket> findByProject_ProjectId(String projectId);
    List<BugTicket> findByFeature_FeatureId(String featureId);
    boolean existsByKey(String key);
    BugTicket findByTitle(String title);
}
