
package com.cloudforge.application.activity;

import com.cloudforge.application.event.ActivityEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ActivityService {

    private final ActivityLogRepository activityLogRepository;

    public ActivityService(
            ActivityLogRepository activityLogRepository) {
        this.activityLogRepository = activityLogRepository;
    }

    @Transactional
    public void recordActivity(ActivityEvent event) {

        if (activityLogRepository.existsByEventId(event.eventId())) {
            return;
        }

        ActivityLog log = new ActivityLog(
                event.eventId(),
                event.eventType(),
                event.userId(),
                event.actorEmail(),
                event.projectId(),
                event.projectName(),
                event.occurredAt()
        );

        activityLogRepository.save(log);
    }


    @Transactional(readOnly = true)
    public Page<ActivityLog> getMyActivity(
            String email,
            String eventType,
            Pageable pageable) {

        if (eventType == null || eventType.isBlank()) {
            return activityLogRepository
                    .findByActorEmailOrderByOccurredAtDesc(email, pageable);
        }

        return activityLogRepository
                .findByActorEmailAndEventTypeOrderByOccurredAtDesc(
                        email, eventType, pageable);
    }
}
