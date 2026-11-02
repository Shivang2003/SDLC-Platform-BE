package com.sdlc.sdlc.dto;

import com.sdlc.sdlc.enums.ProjectRoles;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProjectMemberResponse {
    private String userId;
    private ProjectRoles role;
}