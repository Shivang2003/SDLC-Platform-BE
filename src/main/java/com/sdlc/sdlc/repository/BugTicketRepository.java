package com.sdlc.sdlc.repository;

import com.sdlc.sdlc.entity.BugTicket;
import com.sdlc.sdlc.entity.Feature;
import com.sdlc.sdlc.entity.Project;
import com.sdlc.sdlc.entity.TestTicket;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface BugTicketRepository  extends MongoRepository<BugTicket, ObjectId> {
    BugTicket findByKey(String key);
    List<BugTicket> findByTestCase(TestTicket testCase);
    List<BugTicket> findByProject_ProjectId(String projectId);
    List<BugTicket> findByFeature_FeatureId(String featureId);
    boolean existsByKey(String key);
    BugTicket findByTitle(String title);
    List<BugTicket> findByTestCase_Id(ObjectId testTicketKey);
    BugTicket findByTitleAndFeature(String title, Feature feature);
    List<BugTicket> findByProjectAndFeature(Project project, Feature feature);
    List<BugTicket> findByProjectIn(List<Project> projects);
    @Query(value = "{ 'key': ?0 }", fields = "{ 'title': 1, '_id': 0 }")
    TicketTitleProjection findTitleByKey(String ticketKey);
    @Query(value = "{ 'project.$id': ?0 }",
            fields = "{ 'key': 1, 'title': 1, '_id': 0 }")
    List<TicketTitleProjection> findMentionValuesByProjectId(ObjectId projectId);
}
