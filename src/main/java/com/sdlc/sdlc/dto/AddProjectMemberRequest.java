package com.sdlc.sdlc.dto;

import lombok.Data;
import lombok.NonNull;

@Data
public class AddProjectMemberRequest {
    String userName;
    String role;
}
