package com.sdlc.sdlc.dto;

import com.sdlc.sdlc.enums.ProjectRoles;
import lombok.Data;
import lombok.NonNull;

@Data
public class AddProjectMemberRequest {
    String userName;
    ProjectRoles role;
}
