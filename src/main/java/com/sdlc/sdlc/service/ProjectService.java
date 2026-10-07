package com.sdlc.sdlc.service;


import com.sdlc.sdlc.entity.Organization;
import com.sdlc.sdlc.entity.OrganizationMember;
import com.sdlc.sdlc.entity.Project;
import com.sdlc.sdlc.entity.ProjectMember;
import com.sdlc.sdlc.repository.ProjectMemberRepository;
import com.sdlc.sdlc.repository.ProjectRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ProjectService {

    @Autowired
    public ProjectRepository projectRepository;

    @Autowired
    public ProjectMemberRepository projectMemberRepository;

    public Project checkProjectNameExists(Organization organization,String projectName) {
        try {
            log.info("Organization name received: '{}'", projectName);
            return projectRepository.findByNameAndOrganization(projectName, organization);
        } catch (Exception e) {
            log.error("Error checking if organization exists: {}", e.getMessage());
            return null;
        }
    }

    public Project createNewProject(Organization organization, Project project, OrganizationMember organizationMember) {
        try {
            project.setOrganization(organization);
            Project savedProject = projectRepository.save(project);
            addMemberToProject(organizationMember, savedProject, "ADMIN");
            return savedProject;
        } catch (Exception e) {
            log.error("Error creating new project: {}", e.getMessage(), e);
            return null;
        }
    }
    public ProjectMember addMemberToProject(OrganizationMember organizationMember, Project project, String role) {
        try {
            if (!organizationMember.getOrganization().getId().equals(project.getOrganization().getId())) {

                throw new IllegalStateException("Organization member does not belong to the project organization"
                );
            }

            ProjectMember existingMember = projectMemberRepository.findByOrganizationMemberAndProject(organizationMember, project);
            if (existingMember != null) {throw new IllegalStateException("User is already a member of this project");}

            ProjectMember projectMember = new ProjectMember();

            projectMember.setOrganizationMember(organizationMember);
            projectMember.setProject(project);
            projectMember.setRole(role);

            return projectMemberRepository.save(projectMember);

        } catch (Exception e) {
            log.error("Error adding member to project: {}", e.getMessage(), e);
            return null;
        }
    }
}
