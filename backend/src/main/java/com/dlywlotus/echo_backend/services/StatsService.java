package com.dlywlotus.echo_backend.services;

import com.dlywlotus.echo_backend.constants.StompConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.user.SimpUserRegistry;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StatsService {
    private final SimpUserRegistry simpUserRegistry;
    private final SimpMessagingTemplate simpMessagingTemplate;

    public void broadcastActiveUserCount() {
        int activeUserCount = simpUserRegistry.getUserCount();
        simpMessagingTemplate.convertAndSend(StompConstants.ACTIVE_USERS_TOPIC, activeUserCount);
    }
}
