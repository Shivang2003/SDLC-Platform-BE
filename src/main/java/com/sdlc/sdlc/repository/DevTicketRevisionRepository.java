package com.sdlc.sdlc.repository;

import com.sdlc.sdlc.entity.DevTicket;
import com.sdlc.sdlc.entity.DevTicketRevision;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface DevTicketRevisionRepository extends MongoRepository<DevTicketRevision, ObjectId> {

    @Query(
            value = "{ 'ticketKey': ?0 }",
            fields = "{ 'ticketKey': 1, 'revisionNumber': 1, " +
                    "'title': 1, 'description': 1, " +
                    "'status': 1, 'priority': 1, 'createdAt': 1, " +
                    "'changedBy': 1, 'changedFields': 1, 'changes': 1, " +
                    "'project': 1, 'feature': 1, 'assignee': 1, 'createdBy': 1 }"
    )
    List<DevTicketRevision> findByTicketKeyOrderByRevisionNumberDesc(String ticketKey);
    List<DevTicketRevision> findByTicketKey(String ticketKey);
}
