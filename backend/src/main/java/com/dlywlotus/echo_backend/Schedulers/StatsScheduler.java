package com.dlywlotus.echo_backend.Schedulers;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.dlywlotus.echo_backend.services.StatsService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class StatsScheduler {
    private final StatsService statsService;

    @Scheduled(fixedRate = 2000)
    public void broadcastActiveUserCount() {
        statsService.broadcastActiveUserCount();
    }
}
