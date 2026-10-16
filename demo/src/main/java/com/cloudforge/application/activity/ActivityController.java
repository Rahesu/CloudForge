
package com.cloudforge.application.activity;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/activity")
public class ActivityController {

    private final ActivityService activityService;

    public ActivityController(ActivityService activityService) {
        this.activityService = activityService;
    }

    @GetMapping
    public Page<ActivityLogResponse> getMyActivity(
            Authentication authentication,
            @PageableDefault(size = 20) Pageable pageable,
            @RequestParam(required = false) String eventType) {

        return activityService.getMyActivity(
                        authentication.getName(),
                        eventType,
                        pageable
                )
                .map(ActivityLogResponse::from);
    }
}
