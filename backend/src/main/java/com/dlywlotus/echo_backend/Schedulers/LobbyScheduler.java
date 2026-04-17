package com.dlywlotus.echo_backend.Schedulers;

import com.dlywlotus.echo_backend.services.LobbyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class LobbyScheduler {
    public final LobbyService lobbyService;

    @Scheduled(fixedRate = 500)
    public void processLobbyQueue() {
        lobbyService.processQueue();
    }
}
