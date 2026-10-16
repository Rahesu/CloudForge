package com.cloudforge.application.dto;

import jakarta.validation.constraints.NotBlank;

public class ProjectFileRequest {

    @NotBlank(message = "File name is required")
    private String fileName;

    @NotBlank(message ="File path is requires")
    private String filePath;

    @NotBlank(message ="file content is required")
    private String content;

    private Long version;



    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }
}
