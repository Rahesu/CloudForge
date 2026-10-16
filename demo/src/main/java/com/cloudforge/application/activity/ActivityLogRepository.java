
package com.cloudforge.application.activity;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ActivityLogRepository extends JpaRepository<ActivityLog, Long> {

    boolean existsByEventId(String eventId);

    Page<ActivityLog> findByActorEmailOrderByOccurredAtDesc(
            String actorEmail,
            Pageable pageable
    );

    Page<ActivityLog> findByActorEmailAndEventTypeOrderByOccurredAtDesc(
            String actorEmail,
            String eventType,
            Pageable pageable
    );
}
