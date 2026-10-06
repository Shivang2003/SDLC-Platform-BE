package com.sdlc.sdlc.entity;

public class ErrorResponse {
    private String error;

    public ErrorResponse(String token) {
        this.error = token;
    }

    public String getError() {
        return error;
    }
}