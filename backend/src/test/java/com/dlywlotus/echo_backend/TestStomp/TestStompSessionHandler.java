package com.dlywlotus.echo_backend.TestStomp;

import jakarta.annotation.Nullable;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;

public class TestStompSessionHandler extends StompSessionHandlerAdapter {
    @Override
    public void afterConnected(@Nullable StompSession session, @Nullable StompHeaders connectedHeaders) {
    }
}