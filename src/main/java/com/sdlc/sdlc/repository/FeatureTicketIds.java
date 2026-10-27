package com.sdlc.sdlc.repository;

import java.util.List;

public interface FeatureTicketIds {
    List<String> getDevTicketId();
    List<String> getTestTicketId();
    List<String> getBugTicketId();
}
