package com.dlywlotus.echo_backend.services;

import com.dlywlotus.echo_backend.constants.RedisConstants;
import com.dlywlotus.echo_backend.constants.StompConstants;
import com.dlywlotus.echo_backend.dtos.ChatRoomEvent;
import com.dlywlotus.echo_backend.enums.RoomEventType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatRoomService {
    private final SimpMessagingTemplate stompTemplate;
    private final RedisTemplate<String, String> redisTemplate;

    public void leaveRoom(String redisKey, String roomId) {
        if (!isInRoom(redisKey, roomId)) return;

        // Remove user session from redis set
        redisTemplate.opsForSet().remove(RedisConstants.getRoomRedisKey(roomId), redisKey);
    
        // Send "DISCONNECT" event to room topic
        ChatRoomEvent roomEvent = new ChatRoomEvent(RoomEventType.DISCONNECT,null, null);
        sendRoomEvent(roomId, roomEvent);

    }

    public void validateRoom(String roomId) {
        // Check if the room exists and has two people in it
        String roomRedisKey = RedisConstants.getRoomRedisKey(roomId);
        if (redisTemplate.hasKey(roomRedisKey) && redisTemplate.opsForSet().size(roomRedisKey) == 2) {
            return;
        }

        // Send "DISCONNECT" event to room topic
        ChatRoomEvent roomEvent = new ChatRoomEvent(RoomEventType.DISCONNECT, null, null);
        sendRoomEvent(roomId, roomEvent);
    }

    public void sendMessageEvent(String redisKey, String content, String roomId) {
        if (!isInRoom(redisKey, roomId)) return;

        //Send "MESSAGE" event to room topic
        String userId = getUserId(redisKey);
        ChatRoomEvent roomEvent = new ChatRoomEvent(RoomEventType.MESSAGE, userId, content);
        sendRoomEvent(roomId, roomEvent);
    }

    public void sendTypingEvent(String redisKey, boolean isTyping, String roomId) {
        if (!isInRoom(redisKey, roomId)) return;

        //Send "TYPING" event to room topic
        String userId = getUserId(redisKey);
        ChatRoomEvent roomEvent = new ChatRoomEvent(RoomEventType.TYPING, userId, String.valueOf(isTyping));
        sendRoomEvent(roomId, roomEvent);
    }

    public boolean isInRoom(String redisKey, String roomId) {
        return redisTemplate.opsForSet().isMember(RedisConstants.getRoomRedisKey(roomId), redisKey);
    }

    public String getUserId(String redisKey) {
        return redisTemplate.<String, String>opsForHash().get(redisKey, RedisConstants.USER_ID_HASH_KEY);
    }

    public void sendRoomEvent(String roomId, ChatRoomEvent roomEvent) {
        stompTemplate.convertAndSend(StompConstants.ROOM_PREFIX + roomId, roomEvent);
    }
}
