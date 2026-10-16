
package com.cloudforge.application.activity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(
        name = "activity_logs",
        indexes = {
                @Index(
                        name = "idx_activity_actor_time",
                        columnList = "actor_email, occurred_at"
                )
        }
)
public class ActivityLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_id", nullable = false, unique = true)
    private String eventId;

    @Column(name = "event_type", nullable = false, length = 50)
    private String eventType;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "actor_email", nullable = false)
    private String actorEmail;

    @Column(name = "project_id")
    private Long projectId;

    @Column(name = "project_name")
    private String projectName;

    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;

    protected ActivityLog() {
    }

    public ActivityLog(
            String eventId,
            String eventType,
            Long userId,
            String actorEmail,
            Long projectId,
            String projectName,
            Instant occurredAt) {

        this.eventId = eventId;
        this.eventType = eventType;
        this.userId = userId;
        this.actorEmail = actorEmail;
        this.projectId = projectId;
        this.projectName = projectName;
        this.occurredAt = occurredAt;
    }

    public Long getId() {
        return id;
    }

    public String getEventId() {
        return eventId;
    }

    public String getEventType() {
        return eventType;
    }

    public Long getUserId() {
        return userId;
    }

    public String getActorEmail() {
        return actorEmail;
    }

    public Long getProjectId() {
        return projectId;
    }

    public String getProjectName() {
        return projectName;
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }
}
