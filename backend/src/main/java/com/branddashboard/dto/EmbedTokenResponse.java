package com.branddashboard.dto;

public class EmbedTokenResponse {
    private String token;

    public EmbedTokenResponse(String token) {
        this.token = token;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
