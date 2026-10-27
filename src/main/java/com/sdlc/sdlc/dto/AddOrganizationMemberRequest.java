package com.sdlc.sdlc.dto;

import com.sdlc.sdlc.enums.OrganizationRoles;
import lombok.Data;

@Data
public class AddOrganizationMemberRequest {
    String orgId;
    String userName;
    OrganizationRoles role;
}
