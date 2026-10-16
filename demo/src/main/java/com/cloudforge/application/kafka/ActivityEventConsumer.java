
package com.cloudforge.application.kafka;

import com.cloudforge.application.activity.ActivityService;
import com.cloudforge.application.event.ActivityEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class ActivityEventConsumer {

    private static final Logger log =
            LoggerFactory.getLogger(ActivityEventConsumer.class);

    private final ActivityService activityService;

    public ActivityEventConsumer(ActivityService activityService) {
        this.activityService = activityService;
    }

    @KafkaListener(topics = "cloudforge-activity")
    public void consumeActivity(ActivityEvent event) {

        activityService.recordActivity(event);

        log.info(
                "Activity saved: type={}, actor={}, projectId={}",
                event.eventType(),
                event.actorEmail(),
                event.projectId()
        );
    }
}
