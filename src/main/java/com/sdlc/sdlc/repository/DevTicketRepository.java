package com.sdlc.sdlc.repository;

import com.sdlc.sdlc.entity.DevTicket;
import com.sdlc.sdlc.entity.Feature;
import com.sdlc.sdlc.entity.Project;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface DevTicketRepository extends MongoRepository<DevTicket, ObjectId> {
    DevTicket findByTitleAndFeature(String title, Feature feature);
    DevTicket findByKey(String key);
    List<DevTicket> findByProjectAndFeature(Project project, Feature feature);
    List<DevTicket> findByProjectIn(List<Project> projects);
}
