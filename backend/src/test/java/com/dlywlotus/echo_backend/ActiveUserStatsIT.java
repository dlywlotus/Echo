package com.dlywlotus.echo_backend;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.reflect.Type;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;

import com.dlywlotus.echo_backend.TestStomp.StompUtils;
import com.dlywlotus.echo_backend.constants.StompConstants;
import com.dlywlotus.echo_backend.services.StatsService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ActiveUserStatsIT {
    @LocalServerPort
    private int serverPort;
    @Autowired
    private StatsService statsService;
    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    private StompSession userOneSession;
    private StompSession userTwoSession;
    private CompletableFuture<Integer> userCountCompletableFuture;


    @BeforeEach
    void beforeEach() throws ExecutionException, InterruptedException {
        // Simulate two users connecting to the websocket
        userOneSession = StompUtils.connect(serverPort, UUID.randomUUID().toString());
        userTwoSession = StompUtils.connect(serverPort, UUID.randomUUID().toString());

        // Subscribe user one to the "new room" topic
        userCountCompletableFuture = new CompletableFuture<>();
        userOneSession.subscribe(StompConstants.ACTIVE_USERS_TOPIC, new StompFrameHandler() {
            @Override
            public @NonNull Type getPayloadType(@NonNull StompHeaders headers) {
                return Integer.class;
            }

            @Override
            public void handleFrame(@NonNull StompHeaders headers, @Nullable Object payload) {
                log.info(">>>>>>>>>>>>>>>>>>>> RECEIVED ACTIVE USER COUNT");
                userCountCompletableFuture.complete((Integer) payload);
            }
        });

    }

    @AfterEach
    void afterEach() {
        // Clean up socket connections
        userOneSession.disconnect();
        userTwoSession.disconnect();

        // Clear redis keys (sessions and the lobby queue)
        redisTemplate.execute((RedisCallback<Object>) connection -> {
            connection.serverCommands().flushDb();
            return null;
        });
    }

    @Test
    public void givenTwoActiveUsers_whenReceiveCountEvent_activeUserCountEqualsTwo() throws ExecutionException, InterruptedException, TimeoutException {
        // Manually trigger stats broadcast
        statsService.broadcastActiveUserCount();
        Integer count = userCountCompletableFuture.get(10, TimeUnit.SECONDS);
        log.info(count.toString());
        assertEquals(2, count);
    }

}
