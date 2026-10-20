package com.sdlc.sdlc.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class FeatureSummaryResponse {
    private String featureId;
    private String name;
    private String description;
}