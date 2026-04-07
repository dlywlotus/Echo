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
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;

import com.dlywlotus.echo_backend.TestStomp.StompUtils;
import com.dlywlotus.echo_backend.constants.StompConstants;
import com.dlywlotus.echo_backend.dtos.ChatRoomEvent;
import com.dlywlotus.echo_backend.dtos.SendMessageRequest;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ChatRoomIT {
    @LocalServerPort
    private int serverPort;
    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;

    @Test
    void givenInChatRoom_whenSendMessage_otherUserReceives() throws ExecutionException, InterruptedException, TimeoutException {
        // STATE SET UP BEGIN

        // Simulate two users connecting to the websocket
        String userOneId = UUID.randomUUID().toString();
        String userTwoId = UUID.randomUUID().toString();
        StompSession userOneSession = StompUtils.connect(serverPort, userOneId);
        StompSession userTwoSession = StompUtils.connect(serverPort, userTwoId);

        UUID roomId = UUID.randomUUID();

        // Subscribe user one to the room's topic
        CompletableFuture<String> messageContentCompletableFuture = new CompletableFuture<>();
        userOneSession.subscribe(StompConstants.ROOM_PREFIX + roomId, new StompFrameHandler() {
            @Override
            public @NonNull Type getPayloadType(@NonNull StompHeaders headers) {
                return ChatRoomEvent.class;
            }

            // Runs when an event is pushed to the room's topic
            @Override
            public void handleFrame(@NonNull StompHeaders headers, @Nullable Object payload) {
                log.info(">>>>>>>>>>>>>>>>>>>> RECEIVED MESSAGE");
                if (payload == null) return;
                messageContentCompletableFuture.complete(((ChatRoomEvent) payload).content());
            }
        });
        // STATE SET UP END

        // Verify that user one receives user two's message
        String stompDestination = "/app/room/" + roomId + "/message";
        userTwoSession.send(stompDestination, new SendMessageRequest("Hello!"));
        String receivedMessage = messageContentCompletableFuture.get(10, TimeUnit.SECONDS);
        assertEquals("Hello!", receivedMessage);

        // Clean up web socket connections
        userOneSession.disconnect();
        userTwoSession.disconnect();
    }
}
