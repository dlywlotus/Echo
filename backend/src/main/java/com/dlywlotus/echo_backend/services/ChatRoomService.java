package com.dlywlotus.echo_backend.services;

import com.dlywlotus.echo_backend.constants.RedisConstants;
import com.dlywlotus.echo_backend.constants.StompConstants;
import com.dlywlotus.echo_backend.dtos.ChatRoomEvent;
import com.dlywlotus.echo_backend.enums.RoomEventType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChatRoomService {
    private final SimpMessagingTemplate stompTemplate;
    private final RedisTemplate<String, String> redisTemplate;

    public void leaveRoom(String redisKey, String roomId) {
        // Delete room entry from redis hash
        redisTemplate.opsForHash().delete(redisKey, RedisConstants.ROOM_ID_HASH_KEY);

        // Send "DISCONNECT" event to room topic
        String userId = redisTemplate.<String, String>opsForHash().get(redisKey, RedisConstants.USER_ID_HASH_KEY);
        ChatRoomEvent roomEvent = new ChatRoomEvent(RoomEventType.DISCONNECT, userId, null, null);
        stompTemplate.convertAndSend(StompConstants.ROOM_PREFIX + roomId, roomEvent);
    }

    public void sendMessageToRoom(String redisKey, String content, String roomId) {
        //Send "MESSAGE" event to room topic
        String userId = redisTemplate.<String, String>opsForHash().get(redisKey, RedisConstants.USER_ID_HASH_KEY);
        ChatRoomEvent roomEvent = new ChatRoomEvent(RoomEventType.MESSAGE, userId, content, null);
        stompTemplate.convertAndSend(StompConstants.ROOM_PREFIX + roomId, roomEvent);
    }
}
