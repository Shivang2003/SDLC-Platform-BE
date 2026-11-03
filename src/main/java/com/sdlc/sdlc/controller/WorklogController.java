package com.sdlc.sdlc.controller;

import com.sdlc.sdlc.dto.*;
import com.sdlc.sdlc.entity.ProjectWorklogConfig;
import com.sdlc.sdlc.service.WorklogService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/worklog")
public class WorklogController {

    @Autowired
    private WorklogService worklogService;

    @PostMapping("/project/{projectId}/initialize")
    public ResponseEntity<?> initializeWorklog(
            @PathVariable String projectId,
            @RequestBody InitializeWorklogRequest request) {

        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userName = authentication.getName();

            ProjectWorklogConfigResponse response = worklogService.initializeWorklog(projectId, userName, request);

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to initialize worklog");
        }
    }

    @PostMapping("/project/{projectId}/submit")
    public ResponseEntity<?> submitWorklog(
            @PathVariable String projectId,
            @RequestBody SubmitWorklogRequest request) {

        try {
            Authentication authentication =
                    SecurityContextHolder.getContext().getAuthentication();

            String userName = authentication.getName();

            WorklogResponse response =
                    worklogService.submitWorklog(projectId, userName, request);

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to submit worklog");
        }
    }

    @GetMapping("/project/{projectId}/recent")
    public ResponseEntity<?> getProjectRecentWorklogs(
            @PathVariable String projectId,
            @RequestParam(required = false) LocalDate reportDate) {

        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userName = authentication.getName();

            ProjectWorklogReportResponse response = worklogService.getProjectRecentWorklogs(projectId, userName, reportDate);

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to fetch project worklogs");
        }
    }

    @PutMapping("/project/{projectId}/update")
    public ResponseEntity<?> updateWorklogConfig(
            @PathVariable String projectId,
            @RequestBody UpdateWorklogConfigRequest request) {

        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userName = authentication.getName();

            ProjectWorklogConfigResponse response = worklogService.updateWorklogConfig(projectId, userName, request);

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to update worklog configuration");
        }
    }

    @GetMapping("/project/{projectId}/exists")
    public ResponseEntity<?> checkWorklogExists(
            @PathVariable String projectId) {

        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userName = authentication.getName();
            Map<String, Object> response = worklogService.checkWorklogExists(projectId);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to check worklog");
        }
    }
}