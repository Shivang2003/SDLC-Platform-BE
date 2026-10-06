package com.sdlc.sdlc.service;


import com.sdlc.sdlc.entity.Organization;
import com.sdlc.sdlc.repository.OrganizationRepository;
import com.sdlc.sdlc.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class OrganizationService {

    @Autowired
    OrganizationRepository organizationRepository;

    public Organization createNewOrganization(Organization organization) {
        try {
            Organization savedOrganization = organizationRepository.save(organization);
            log.info("New organization created: {}", savedOrganization.getName());
            return savedOrganization;
        } catch (Exception e) {
            log.error("Error creating new organization: {}", e.getMessage());
            return null;
        }
    }
}
