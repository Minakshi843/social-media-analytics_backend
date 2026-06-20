package com.social.analytics.dto;

import jakarta.validation.constraints.NotBlank;

public class CityRequest {

    @NotBlank(message = "City name is required")
    private String name;

    public CityRequest() {
    }

    public CityRequest(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
