
package com.cloudforge.application.activity;

import java.time.Instant;

public record ActivityLogResponse(
        Long id,
        String eventType,
        String actorEmail,
        Long projectId,
        String projectName,
        Instant occurredAt
) {
    public static ActivityLogResponse from(ActivityLog log) {
        return new ActivityLogResponse(
                log.getId(),
                log.getEventType(),
                log.getActorEmail(),
                log.getProjectId(),
                log.getProjectName(),
                log.getOccurredAt()
        );
    }
}
