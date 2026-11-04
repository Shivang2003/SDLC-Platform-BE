package com.sdlc.sdlc.repository;

import com.sdlc.sdlc.entity.Worklog;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface WorklogRepository extends MongoRepository<Worklog, String> {

    boolean existsByProjectIdAndUserIdAndWorkDate(String projectId, String userId, LocalDate workDate);
    List<Worklog> findByProjectIdAndSubmittedAtGreaterThanEqualAndSubmittedAtLessThanOrderBySubmittedAtDesc(String projectId, LocalDateTime start, LocalDateTime end);
}
