package com.dlywlotus.echo_backend.controllers;

import java.util.Objects;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.RestController;

import com.dlywlotus.echo_backend.Exceptions.WebSocketException;
import com.dlywlotus.echo_backend.dtos.JoinRoomRequest;
import com.dlywlotus.echo_backend.dtos.RoomEvent;
import com.dlywlotus.echo_backend.dtos.SendMessageRequest;
import com.dlywlotus.echo_backend.enums.RoomEventType;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class WebSocketController {
    private final RedisTemplate<String, String> redisTemplate;
    private final SimpMessagingTemplate stompTemplate;

    @MessageMapping("/lobby/join")
    public void joinLobby(@Payload JoinRoomRequest request,
                          SimpMessageHeaderAccessor simpMessageHeaderAccessor) {
        String redisKey = getRedisKey(simpMessageHeaderAccessor);

        // Add session to the lobby
        redisTemplate.opsForList().rightPush("list:waiting_sessions", redisKey);

        // Set username for session
        redisTemplate.opsForHash().put(redisKey, "username", request.username());
    }

    @MessageMapping("/room/{roomId}/leave")
    public void leaveRoom(@DestinationVariable String roomId,
                          SimpMessageHeaderAccessor simpMessageHeaderAccessor) {
        String redisKey = getRedisKey(simpMessageHeaderAccessor);

        // Delete room entry from redis hash
        redisTemplate.opsForHash().delete(redisKey, "roomId");

        // Send "DISCONNECT" event to room topic
        String userId = redisTemplate.<String, String>opsForHash().get(redisKey, "userId");
        RoomEvent roomEvent = new RoomEvent(RoomEventType.DISCONNECT, userId, null, null);
        stompTemplate.convertAndSend("/topic/room/" + roomId, roomEvent);
    }

    @MessageMapping("/room/{roomId}/send")
    public void sendMessage(@Payload SendMessageRequest request, @DestinationVariable String roomId,
                            SimpMessageHeaderAccessor simpMessageHeaderAccessor) {
        String redisKey = getRedisKey(simpMessageHeaderAccessor);

        //Send "MESSAGE" event to room topic
        String userId = redisTemplate.<String, String>opsForHash().get(redisKey, "userId");
        RoomEvent roomEvent = new RoomEvent(RoomEventType.MESSAGE, userId, request.content(), null);
        stompTemplate.convertAndSend("/topic/room/" + roomId, roomEvent);
    }

    private String getRedisKey(SimpMessageHeaderAccessor simpMessageHeaderAccessor) {
        String sessionId = simpMessageHeaderAccessor.getSessionId();
        if (Objects.isNull(sessionId)) {
            throw new WebSocketException("Invalid web socket session id");
        }
        return "session:" + sessionId;
    }
}
