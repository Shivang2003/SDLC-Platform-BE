package com.sdlc.sdlc.service;


import com.sdlc.sdlc.entity.Organization;
import com.sdlc.sdlc.repository.OrganizationMemberRepository;
import com.sdlc.sdlc.repository.OrganizationRepository;
import com.sdlc.sdlc.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.sdlc.sdlc.entity.User;
import com.sdlc.sdlc.entity.OrganizationMember;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class OrganizationService {

    @Autowired
    OrganizationRepository organizationRepository;

    @Autowired
    OrganizationMemberRepository organizationMemberRepository;

    @Autowired
    UserRepository userRepository;

    public Organization createNewOrganization(Organization organization, User user) {
        Organization savedOrganization = organizationRepository.save(organization);
        addMemberToOrganization(savedOrganization, user, "OWNER");
        log.info("New organization created: {}", savedOrganization.getName());
        return savedOrganization;
    }

    public OrganizationMember addMemberToOrganization(Organization organization, User user, String role) {
        OrganizationMember existingMember = organizationMemberRepository.findByUserAndOrganization(user, organization);
        if (existingMember != null) {
            throw new IllegalStateException("User is already a member of this organization");
        }
        OrganizationMember organizationMember = new OrganizationMember();
        organizationMember.setOrganization(organization);
        organizationMember.setUser(user);
        organizationMember.setRole(role);
        return organizationMemberRepository.save(organizationMember);
    }

    public Organization checkOrganizationExists(String organizationName) {
        try {
            log.info("Organization name received: '{}'", organizationName);
            return organizationRepository.findByName(organizationName);
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
