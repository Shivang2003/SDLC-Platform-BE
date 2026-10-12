package com.sdlc.sdlc.service;

import com.sdlc.sdlc.entity.Feature;
import com.sdlc.sdlc.entity.Project;
import com.sdlc.sdlc.entity.ProjectMember;
import com.sdlc.sdlc.enums.FeatureStatus;
import com.sdlc.sdlc.enums.ProjectRoles;
import com.sdlc.sdlc.repository.FeatureRepositroy;
import com.sdlc.sdlc.repository.ProjectMemberRepository;
import com.sdlc.sdlc.repository.ProjectRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Service
@Slf4j
public class FeatureService {

    @Autowired
    ProjectRepository projectRepository;

    @Autowired
    ProjectMemberRepository projectMemberRepository;

    @Autowired
    FeatureRepositroy featureRepository;

    @Autowired
    IdGeneratorService idGeneratorService;

    public ProjectMember validateFeatureEditAccess(String projectId, String userName, Set<ProjectRoles> restrictedRoles) {
        Project project = projectRepository.findByProjectId(projectId);
        if (project == null) {
            return null;
        }
        List<ProjectMember> members = projectMemberRepository.findByProject_Id(project.getId());

        ProjectMember member = members.stream()
                .filter(m -> m.getOrganizationMember() != null)
                .filter(m -> m.getOrganizationMember().getUser() != null)
                .filter(m -> userName.equals(m.getOrganizationMember().getUser().getUserName()))
                .findFirst()
                .orElseThrow(() -> new AccessDeniedException("You are not a member of this project"));

        ProjectRoles userRole = ProjectRoles.valueOf(member.getRole());
        if (restrictedRoles.contains(userRole)) {
            throw new AccessDeniedException(
                    "Your role " + userRole + " does not have permission to perform this action");
        }
        return member;
    }

    public Feature checkIfFeatureNameExists(String Name, Project project){
        try{
            Feature feature = featureRepository.findByNameAndProject(Name, project);
            return feature;
        }
        catch (Exception e){
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error checking if feature exists", e);
        }
    }

    public Feature checkIfFeatureIdExists(String featureId){
        try{
            Feature feature = featureRepository.findByFeatureId(featureId);
            return feature;
        }
        catch (Exception e){
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error checking if feature exists", e);
        }

    }

    public Feature createNewFeature(Feature feature, Project project, ProjectMember createdBy){
        try {
            LocalDateTime now = LocalDateTime.now();

            feature.setId(null);
            feature.setKey(idGeneratorService.generateSequenceId("feature", "FTR"));
            feature.setFeatureId(feature.getKey());
            feature.setProject(project);
            feature.setCreatedBy(createdBy);

            if (feature.getStatus() == null) {
                feature.setStatus(FeatureStatus.PLANNED);
            }

            feature.setCreatedAt(now);
            feature.setUpdatedAt(now);

            return featureRepository.save(feature);

        } catch (Exception e) {
            throw new RuntimeException("Failed to create feature", e);
        }
    }
}
