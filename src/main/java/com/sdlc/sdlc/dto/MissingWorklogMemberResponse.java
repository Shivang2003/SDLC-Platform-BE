package com.sdlc.sdlc.dto;

import com.sdlc.sdlc.enums.ProjectRoles;
import lombok.Data;

@Data
public class MissingWorklogMemberResponse {
    private String userId;
    private String userName;
    private ProjectRoles projectRole;
}
