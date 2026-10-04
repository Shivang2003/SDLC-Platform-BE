package com.sdlc.sdlc.entity;

import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@Document(collection = "test_cases")
public class TestCase {

    @Id
    private ObjectId id;

    private String testcaseId;
    private String title;
    private String description;

    private List<String> steps;

    private String expectedResult;
    private String status;

    @DBRef
    private DevTicket devTicket;

    @DBRef
    private User createdBy;
}
