package com.dlywlotus.echo_backend.Schedulers;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

//TODO: Find out how to test this application
//TODO: Add scheduler that pops from redis list (Make sure the schedulers check if the users have
// disconnected or not before adding them to the room)
//TODO: Add a scheduler that gets the number of active users from the number of active sessions ->
// and broadcasts to all users "topic/global/stats"

@Slf4j
@Component
public class LobbyScheduler {

    @Scheduled(fixedRate = 1000)
    public void performTask() {
        log.info("hello");
    }
}
