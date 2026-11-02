package com.sdlc.sdlc.dto;

import com.sdlc.sdlc.enums.ProjectRoles;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ChangeProjectMemberRoleRequest {
    private String userName;
    private ProjectRoles newRole;
}
