package com.sdlc.sdlc.repository;

import com.sdlc.sdlc.entity.TicketComments;
import com.sdlc.sdlc.enums.TicketType;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface TicketCommentsRepository extends MongoRepository<TicketComments, String> {
    Optional<TicketComments> findByTicketKeyAndTicketType(String ticketKey, TicketType ticketType);
}
