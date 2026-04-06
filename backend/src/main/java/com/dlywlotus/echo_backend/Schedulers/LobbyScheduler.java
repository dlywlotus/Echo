package com.dlywlotus.echo_backend.Schedulers;

import com.dlywlotus.echo_backend.services.LobbyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

//TODO: Add integration tests for main components
// 1. queue processer

//TODO: Add real time "is typing" notification"

//TODO: Have the FE send pings every 10s, if the RTT of the pings are too long, display that the user has poor internet on
// the FE.

@Slf4j
@Component
@RequiredArgsConstructor
public class LobbyScheduler {
    public final LobbyService lobbyService;

    @Scheduled(fixedRate = 1000)
    public void processLobbyQueue() {
        lobbyService.processQueue();
    }
}
