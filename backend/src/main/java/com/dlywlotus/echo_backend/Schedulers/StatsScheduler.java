package com.dlywlotus.echo_backend.Schedulers;

import com.dlywlotus.echo_backend.services.StatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StatsScheduler {
    private final StatsService statsService;

    @Scheduled(fixedRate = 10000)
    public void broadcastActiveUserCount() {
        statsService.broadcastActiveUserCount();
    }
}
