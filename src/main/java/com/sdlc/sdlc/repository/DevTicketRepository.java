package com.sdlc.sdlc.repository;

import com.sdlc.sdlc.entity.DevTicket;
import com.sdlc.sdlc.entity.Feature;
import com.sdlc.sdlc.entity.Project;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;
import java.util.Optional;

public interface DevTicketRepository extends MongoRepository<DevTicket, ObjectId> {
    DevTicket findByTitleAndFeature(String title, Feature feature);
    DevTicket findByKey(String key);
    @Query("{ 'project.$id': ?0, 'feature.$id': ?1 }")
    List<DevTicket> findByProjectIdAndFeatureId(ObjectId projectId, ObjectId featureId);
    List<DevTicket> findByProjectAndFeature(Project project, Feature feature);
    List<DevTicket> findByProjectIn(List<Project> projects);
    @Query(value = "{ 'key': ?0 }", fields = "{ 'title': 1, '_id': 0 }")
    TicketTitleProjection findTitleByKey(String ticketKey);
    @Query(value = "{ 'project.$id': ?0 }",
            fields = "{ 'key': 1, 'title': 1, '_id': 0 }")
    List<TicketTitleProjection> findMentionValuesByProjectId(ObjectId projectId);
}
