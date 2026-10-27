package com.sdlc.sdlc.service;


import com.sdlc.sdlc.entity.Organization;
import com.sdlc.sdlc.enums.OrganizationRoles;
import com.sdlc.sdlc.repository.OrganizationMemberRepository;
import com.sdlc.sdlc.repository.OrganizationRepository;
import com.sdlc.sdlc.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.sdlc.sdlc.entity.User;
import com.sdlc.sdlc.entity.OrganizationMember;

import java.util.ArrayList;

@Service
@Slf4j
public class OrganizationService {

    @Autowired
    OrganizationRepository organizationRepository;

    @Autowired
    OrganizationMemberRepository organizationMemberRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    IdGeneratorService idGeneratorService;

    public Organization createNewOrganization(Organization organization, User user) {
        organization.setOrganizationId(idGeneratorService.generateSequenceId("organization", "ORG"));
        organization.setProjectId(null);
        if (organization.getUserId() == null) {
            organization.setUserId(new ArrayList<>());
        }
        organization.getUserId().add(user.getUserId());
        Organization savedOrganization = organizationRepository.save(organization);
        addMemberToOrganization(savedOrganization, user, OrganizationRoles.OWNER);
        log.info("New organization created: {}", savedOrganization.getName());
        return savedOrganization;
    }

    public OrganizationMember addMemberToOrganization(Organization organization, User user, OrganizationRoles role) {
        OrganizationMember existingMember = organizationMemberRepository.findByUserAndOrganization(user, organization);
        if (existingMember != null) {
            throw new IllegalStateException("User is already a member of this organization");
        }
        OrganizationMember organizationMember = new OrganizationMember();
        organizationMember.setOrganization(organization);
        organizationMember.setUser(user);
        organizationMember.setRole(role);
        organizationMember.setUserId(user.getUserId());
        organizationMember.setUserId(user.getUserId());
        return organizationMemberRepository.save(organizationMember);
    }

    public Organization checkOrganizationNameExists(String organizationName) {
        try {
            log.info("Organization name received: '{}'", organizationName);
            return organizationRepository.findByName(organizationName);
        } catch (Exception e) {
            log.error("Error checking if organization exists: {}", e.getMessage());
            return null;
        }
    }

    public Organization checkOrganizationIdExists(String organizationId) {
        try {
//            ObjectId orgId = new ObjectId(organizationId);
            return organizationRepository.findByOrganizationId(organizationId);
        } catch (Exception e) {
            log.error("Error checking if organization exists: {}", e.getMessage());
            return null;
        }
    }

    public OrganizationMember findOrganizationMemberByUserName(String username, Organization organization) {
        User user = userRepository.findByUserName(username);
        if (user == null) {
            return null;
        }
        return organizationMemberRepository.findByUserAndOrganization(user, organization);
    }

}
