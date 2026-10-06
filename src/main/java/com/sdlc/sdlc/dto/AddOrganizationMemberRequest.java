package com.sdlc.sdlc.dto;

import lombok.Data;

@Data
public class AddOrganizationMemberRequest {
    String organizationName;
    String userName;
}
