package com.sdlc.sdlc.dto;

import com.sdlc.sdlc.enums.OrganizationRoles;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrganizationMemberResponse {
    private String userId;
    private OrganizationRoles role;
}
