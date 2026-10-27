
package com.sdlc.sdlc.repository;

import com.sdlc.sdlc.entity.MentionConfig;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface MentionConfigRepository
        extends MongoRepository<MentionConfig, String> {
}
