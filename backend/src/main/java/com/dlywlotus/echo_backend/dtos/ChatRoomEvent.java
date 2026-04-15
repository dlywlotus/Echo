package com.dlywlotus.echo_backend.dtos;

import com.dlywlotus.echo_backend.enums.RoomEventType;
import lombok.Getter;

import java.time.Instant;

@Getter
public class ChatRoomEvent {
    public RoomEventType type;
    public String userId;
    public String content;
    public Instant timestamp;

    public ChatRoomEvent(RoomEventType type, String userId, String content) {
        this.type = type;
        this.userId = userId;
        this.content = content;
        this.timestamp = Instant.now();
    }
}
