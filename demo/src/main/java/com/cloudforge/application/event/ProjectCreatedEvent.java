package com.cloudforge.application.event;

public record ProjectCreatedEvent(
        Long projectId,
        String projectName,
        String createdBy
) {
}
