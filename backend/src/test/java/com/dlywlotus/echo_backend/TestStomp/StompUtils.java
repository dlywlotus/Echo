package com.dlywlotus.echo_backend.TestStomp;

import org.springframework.messaging.converter.JacksonJsonMessageConverter;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandler;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class StompUtils {
    public static StompSession connect(int serverPort, String userId) throws ExecutionException, InterruptedException {
        // Set up web socket connection
        String url = "ws://localhost:" + serverPort + "/web-socket";
        WebSocketStompClient stompClient = new WebSocketStompClient(new StandardWebSocketClient());
        stompClient.setMessageConverter(new JacksonJsonMessageConverter());

        // Set user-id in header (FE should do this)
        StompHeaders connectHeaders = new StompHeaders();
        connectHeaders.add("user-id", userId);
        StompSessionHandler sessionHandler = new TestStompSessionHandler();

        // Connect to web socket
        CompletableFuture<StompSession> completableFuture = stompClient.connectAsync(
                url,
                (WebSocketHttpHeaders) null,
                connectHeaders,
                sessionHandler);

        return completableFuture.get();
    }
}
