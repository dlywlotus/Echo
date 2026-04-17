package com.dlywlotus.echo_backend;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;
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
import org.springframework.test.context.ActiveProfiles;

import com.dlywlotus.echo_backend.TestStomp.StompUtils;
import com.dlywlotus.echo_backend.constants.StompConstants;
import com.dlywlotus.echo_backend.dtos.JoinRoomRequest;
import com.dlywlotus.echo_backend.dtos.RoomDetails;
import com.dlywlotus.echo_backend.services.LobbyService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("local")
class LobbyIT {
    @LocalServerPort
    private int serverPort;
    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    @Autowired
    private LobbyService lobbyService;
    private String userOneId;
    private String userTwoId;
    private StompSession userOneSession;
    private StompSession userTwoSession;
    private CompletableFuture<RoomDetails> roomResultCompletableFuture;

    @BeforeEach
    void beforeEach() throws ExecutionException, InterruptedException {
        // Simulate two users connecting to the websocket
        userOneId = UUID.randomUUID().toString();
        userTwoId = UUID.randomUUID().toString();
        userOneSession = StompUtils.connect(serverPort, userOneId);
        userTwoSession = StompUtils.connect(serverPort, userTwoId);

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
    void givenTwoUsersInQueue_whenSchedulerTriggers_createChatRoom() throws ExecutionException, InterruptedException, TimeoutException {
        // Subscribe user one to the "new room" topic
        roomResultCompletableFuture = new CompletableFuture<>();
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

        // Assert that scheduler successfully popped the two users from the queue, created a room
        // and sent the new room event to user one.
        RoomDetails roomResult = roomResultCompletableFuture.get(10, TimeUnit.SECONDS);
        Set<String> roomUserIds = new HashSet<>();
        roomUserIds.add(roomResult.userOneId());
        roomUserIds.add(roomResult.userTwoId());
        assertTrue(roomUserIds.contains(userOneId) && roomUserIds.contains(userTwoId));

        // Also make sure that any already processed user can still join the queue once more
        userOneSession.send("/app/lobby/join", new JoinRoomRequest("Alice"));
        // Polls the service until the condition is met
        await().atMost(5, TimeUnit.SECONDS).untilAsserted(
                () -> assertEquals(1, lobbyService.getQueueSize())
        );
    }

    @Test
    public void givenUserAlreadyInLobby_whenJoinLobbyAgain_userIsNotAddedToLobby() {
        userOneSession.send("/app/lobby/join", new JoinRoomRequest("Alice"));

        // Wait for first lobby join then join again
        await().atMost(2, TimeUnit.SECONDS).until(() -> lobbyService.getQueueSize() == 1);
        userOneSession.send("/app/lobby/join", new JoinRoomRequest("Alice"));

        // Check that the queue size remains 1
        await().atMost(4, TimeUnit.SECONDS)
                .during(2, TimeUnit.SECONDS)
                .until(() -> lobbyService.getQueueSize() == 1);
    }
}
