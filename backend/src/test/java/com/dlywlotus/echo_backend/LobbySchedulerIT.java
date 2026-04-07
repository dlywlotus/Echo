package com.dlywlotus.echo_backend;

import com.dlywlotus.echo_backend.TestStomp.StompUtils;
import com.dlywlotus.echo_backend.constants.StompConstants;
import com.dlywlotus.echo_backend.dtos.JoinRoomRequest;
import com.dlywlotus.echo_backend.dtos.RoomDetails;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
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

import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class LobbySchedulerIT {
    @LocalServerPort
    private int serverPort;
    @Autowired
    private RedisTemplate<String, String> redisTemplate;


    @BeforeEach
    void afterEach() {
        redisTemplate.execute((RedisCallback<Object>) connection -> {
            connection.serverCommands().flushDb();
            return null;
        });
    }

    @Test
    void givenTwoUsersInQueue_whenSchedulerTriggers_createChatRoom() throws InterruptedException, ExecutionException, TimeoutException {
        String userOneId = UUID.randomUUID().toString();
        String userTwoId = UUID.randomUUID().toString();
        StompSession userOneSession = StompUtils.connect(serverPort, userOneId);
        StompSession userTwoSession = StompUtils.connect(serverPort, userTwoId);

        // Subscribe to the "new room" topic for user one
        CompletableFuture<RoomDetails> roomResultCompletableFuture = new CompletableFuture<>();
        userOneSession.subscribe(StompConstants.getUserNewRoomTopic(userOneId), new StompFrameHandler() {
            @Override
            public @NonNull Type getPayloadType(@NonNull StompHeaders headers) {
                return RoomDetails.class;
            }

            @Override
            public void handleFrame(@NonNull StompHeaders headers, @Nullable Object payload) {
                log.info(">>>>>>>>>>>>>>>>>>>> RECEIVED NEW ROOM EVENT");
                roomResultCompletableFuture.complete((RoomDetails) payload);
            }
        });

        // Add both users to the lobby
        userOneSession.send("/app/lobby/join", new JoinRoomRequest("Alice"));
        userTwoSession.send("/app/lobby/join", new JoinRoomRequest("Bob"));

        // Assert that both users ids are in the created room
        RoomDetails roomResult = roomResultCompletableFuture.get(10, TimeUnit.SECONDS);
        Set<String> roomUserIds = new HashSet<>();
        roomUserIds.add(roomResult.userOneId());
        roomUserIds.add(roomResult.userTwoId());
        assertTrue(roomUserIds.contains(userOneId) && roomUserIds.contains(userTwoId));
    }
}
