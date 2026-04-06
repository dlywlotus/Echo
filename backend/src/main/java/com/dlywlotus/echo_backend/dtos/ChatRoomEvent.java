package com.dlywlotus.echo_backend.dtos;

import com.dlywlotus.echo_backend.enums.RoomEventType;

public record ChatRoomEvent(RoomEventType type, String userId, String content, Boolean isTyping) {
}
