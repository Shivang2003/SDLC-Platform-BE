package com.sdlc.sdlc.service;

import com.sdlc.sdlc.model.Counter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class IdGeneratorService {

    private final MongoTemplate mongoTemplate;

    public String generateSequenceId(String entity) {
        Query query = new Query(Criteria.where("_id").is(entity));

        Update update = new Update().inc("sequence", 1);

        FindAndModifyOptions options = FindAndModifyOptions.options()
                .returnNew(true)
                .upsert(true);

        Counter counter = mongoTemplate.findAndModify(
                query,
                update,
                options,
                Counter.class
        );

        return "PRJ-" + counter.getSequence();
    }
}