package com.cloudforge.application.event;

import java.time.Instant;

    public record ActivityEvent(
            String eventId,
            String eventType,
            Long userId,
            String actorEmail,
            Long projectId,
            String projectName,
            Instant occurredAt
    ) {

    }

