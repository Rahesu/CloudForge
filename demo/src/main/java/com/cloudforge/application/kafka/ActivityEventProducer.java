
package com.cloudforge.application.kafka;

import com.cloudforge.application.event.ActivityEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Component
public class ActivityEventProducer {

    private static final Logger log =
            LoggerFactory.getLogger(ActivityEventProducer.class);

    private static final String TOPIC = "cloudforge-activity";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public ActivityEventProducer(
            KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishLogin(Long userId, String email) {
        ActivityEvent event = new ActivityEvent(
                UUID.randomUUID().toString(),
                "USER_LOGGED_IN",
                userId,
                email,
                null,
                null,
                Instant.now()
        );

        publish(event, email);
    }

    public void publishProjectCreated(
            Long projectId,
            String projectName,
            Long userId,
            String email) {

        ActivityEvent event = new ActivityEvent(
                UUID.randomUUID().toString(),
                "PROJECT_CREATED",
                userId,
                email,
                projectId,
                projectName,
                Instant.now()
        );

        publish(event, String.valueOf(projectId));
    }


    private void publish(ActivityEvent event, String key) {
        try {
            kafkaTemplate.send(TOPIC, key, event)
                    .whenComplete((result, exception) -> {
                        if (exception != null) {
                            log.error(
                                    "Failed to publish activity event: eventId={}, type={}",
                                    event.eventId(),
                                    event.eventType(),
                                    exception
                            );
                        } else {
                            log.info(
                                    "Activity event published: eventId={}, type={}",
                                    event.eventId(),
                                    event.eventType()
                            );
                        }
                    });

        } catch (Exception exception) {
            log.error(
                    "Kafka send failed immediately: eventId={}, type={}",
                    event.eventId(),
                    event.eventType(),
                    exception
            );
        }
    }

}
