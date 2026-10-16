package com.cloudforge.application.dto;

import jakarta.validation.constraints.NotBlank;

public class ApplicationRequest {

    @NotBlank
    private String name;

    @NotBlank
    private String description;

    @NotBlank
    private String runtime;

    @NotBlank
    private String version;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getRuntime() {
        return runtime;
    }

    public void setRuntime(String runtime) {
        this.runtime = runtime;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }
}
