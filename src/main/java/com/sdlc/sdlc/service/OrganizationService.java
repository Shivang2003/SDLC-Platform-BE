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
        OrganizationMember member = addMemberToOrganization(organization, user, "OWNER");
        organization.setMembers(List.of(member));
        Organization savedOrganization = organizationRepository.save(organization);
        log.info("New organization created: {}", savedOrganization.getName());
        return savedOrganization;
    }

    public OrganizationMember addMemberToOrganization(Organization organization, User user, String role) {
        OrganizationMember organizationMember = new OrganizationMember();
        organizationMember.setUser(user);
        organizationMember.setRole(role);
        organizationMember = organizationMemberRepository.save(organizationMember);
        if (organization.getMembers() == null) {
            organization.setMembers(new ArrayList<>());
        }
        organization.getMembers().add(organizationMember);
        organizationRepository.save(organization);
        return organizationMember;
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

    public OrganizationMember findOrganizationMemberByUserName(String username) {
        User user = userRepository.findByUserName(username);
        if (user == null) {
            return null;
        }
        return organizationMemberRepository.findByUser(user);
    }

}
