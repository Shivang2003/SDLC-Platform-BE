package com.sdlc.sdlc.repository;

import com.sdlc.sdlc.entity.TestTicketBugTicketLink;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface TestTicketBugTicketLinkRepository
        extends MongoRepository<TestTicketBugTicketLink, String> {

    List<TestTicketBugTicketLink> findByTestTicketKey(String testTicketKey);

    List<TestTicketBugTicketLink> findByBugTicketKey(String bugTicketKey);

    boolean existsByTestTicketKeyAndBugTicketKey(
            String testTicketKey,
            String bugTicketKey
    );
}