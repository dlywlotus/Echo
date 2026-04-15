package com.dlywlotus.echo_backend;

import com.dlywlotus.echo_backend.TestStomp.StompUtils;
import com.dlywlotus.echo_backend.constants.RedisConstants;
import com.dlywlotus.echo_backend.constants.StompConstants;
import com.dlywlotus.echo_backend.dtos.ChatRoomEvent;
import com.dlywlotus.echo_backend.dtos.SendMessageRequest;
import com.dlywlotus.echo_backend.enums.RoomEventType;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.user.SimpUserRegistry;

import java.lang.reflect.Type;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ChatRoomIT {
    @LocalServerPort
    private int serverPort;
    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;
    @Autowired
    private SimpUserRegistry simpUserRegistry;
    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @AfterEach
    void afterEach() {
        // Clear redis keys (sessions and the lobby queue)
        redisTemplate.execute((RedisCallback<Object>) connection -> {
            connection.serverCommands().flushDb();
            return null;
        });
    }

    @Test
    void givenInChatRoom_whenSendMessage_otherUserReceives() throws InterruptedException, TimeoutException, ExecutionException {
        // Simulate two users connecting to the websocket
        StompSession userOneSession = StompUtils.connect(serverPort, UUID.randomUUID().toString());
        StompSession userTwoSession = StompUtils.connect(serverPort, UUID.randomUUID().toString());

        // Simulate two users joining a room (w/o using the lobby scheduler and join room methods)
        String roomId = UUID.randomUUID().toString();
        simpUserRegistry.getUsers().forEach(user -> {
            if (user.getPrincipal() == null) return;
            redisTemplate.opsForSet().add(RedisConstants.getRoomRedisKey(roomId), user.getName());
        });

        // Subscribe user one to the room's topic
        CompletableFuture<ChatRoomEvent> messageContentCompletableFuture = new CompletableFuture<>();
        userOneSession.subscribe(StompConstants.ROOM_PREFIX + roomId, new StompFrameHandler() {
            @Override
            public @NonNull Type getPayloadType(@NonNull StompHeaders headers) {
                return ChatRoomEvent.class;
            }

            // Runs when an event is pushed to the room's topic
            @Override
            public void handleFrame(@NonNull StompHeaders headers, @Nullable Object payload) {
                log.info(">>>>>>>>>>>>>>>>>>>> RECEIVED CHAT MESSAGE");
                if (payload == null) return;
                messageContentCompletableFuture.complete(((ChatRoomEvent) payload));
            }
        });

        // Verify that user one receives user two's message
        String stompDestination = "/app/room/" + roomId + "/message";
        userTwoSession.send(stompDestination, new SendMessageRequest("Hello!"));
        String receivedMessage = messageContentCompletableFuture.get(10, TimeUnit.SECONDS).getContent();
        assertEquals("Hello!", receivedMessage);

        // Clean up web socket connections
        userOneSession.disconnect();
        userTwoSession.disconnect();
    }

    @Test
    void givenUserTwoLeft_AfterRoomCreated_userOneReceivesDisconnectEvent() throws ExecutionException, InterruptedException, TimeoutException {
        String userOneId = UUID.randomUUID().toString();
        StompSession userOneSession = StompUtils.connect(serverPort, userOneId);

        // Simulate room with only user one in it (w/o using the lobby scheduler and join room methods)
        String roomId = UUID.randomUUID().toString();
        simpUserRegistry.getUsers().forEach(user -> {
            if (user.getPrincipal() == null) return;
            redisTemplate.opsForSet().add(RedisConstants.getRoomRedisKey(roomId), user.getName());
        });

        // Subscribe user one to the room's topic
        CompletableFuture<ChatRoomEvent> messageContentCompletableFuture = new CompletableFuture<>();
        userOneSession.subscribe(StompConstants.ROOM_PREFIX + roomId, new StompFrameHandler() {
            @Override
            public @NonNull Type getPayloadType(@NonNull StompHeaders headers) {
                return ChatRoomEvent.class;
            }

            @Override
            public void handleFrame(@NonNull StompHeaders headers, @Nullable Object payload) {
                log.info(">>>>>>>>>>>>>>>>>>>> RECEIVED DISCONNECT EVENT");
                if (payload == null) return;
                messageContentCompletableFuture.complete(((ChatRoomEvent) payload));
            }
        });

        // Validate if room has two people
        String stompDestination = "/app/room/" + roomId + "/validate";
        userOneSession.send(stompDestination, new ChatRoomEvent(null, null, null));

        // Verify that user one receives user two's DISCONNECT event
        RoomEventType roomEventType = messageContentCompletableFuture.get(10, TimeUnit.SECONDS).getType();
        assertEquals(RoomEventType.DISCONNECT, roomEventType);

        // Clean up web socket connections
        userOneSession.disconnect();
    }

}
