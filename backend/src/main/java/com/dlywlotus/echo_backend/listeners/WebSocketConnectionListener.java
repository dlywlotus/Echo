package com.dlywlotus.echo_backend.listeners;

import com.dlywlotus.echo_backend.constants.RedisConstants;
import com.dlywlotus.echo_backend.constants.StompConstants;
import com.dlywlotus.echo_backend.dtos.ChatRoomEvent;
import com.dlywlotus.echo_backend.enums.RoomEventType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketConnectionListener {
    private final RedisTemplate<String, String> redisTemplate;
    private final SimpMessagingTemplate stompTemplate;

    //This may not work (please verify with tests)
    @EventListener
    public void handleSessionConnect(SessionConnectedEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();

        if (sessionId == null) return;

        // Extract User ID from the headers sent by the Frontend
        // Note: "user-id" must match key sent in "connectHeaders"
        String userId = headerAccessor.getFirstNativeHeader("user-id");
        log.info(">>>>>>>>>>>>>>>>>>The user id set on connect :{}", userId);

        // Set the userId in the redis hash
        String redisKey = "session:" + sessionId;
        redisTemplate.opsForHash().put(redisKey, "userId", userId);

        // Set a TTL of 1 hour in case the server crashes and session disconnect event doesnt fire
        redisTemplate.expire(redisKey, 1, TimeUnit.HOURS);

    }

    @EventListener
    public void handleSessionDisconnect(SessionDisconnectEvent event) {
        String redisKey = "session:" + event.getSessionId();

        //Remove session from lobby if the user was in it
        redisTemplate.opsForList().remove(RedisConstants.LOBBY_KEY, 1, redisKey);

        // Send "DISCONNECT" event to room topic if the user was in a room
        HashOperations<String, String, String> hashOperations = redisTemplate.opsForHash();
        String roomId = hashOperations.get(redisKey, "roomId");
        if (!Objects.isNull(roomId)) {
            String userId = hashOperations.get(redisKey, "userId");
            ChatRoomEvent roomEvent = new ChatRoomEvent(RoomEventType.DISCONNECT, userId, null, null);
            stompTemplate.convertAndSend(StompConstants.ROOM_PREFIX + roomId, roomEvent);
        }

        //Remove session from redis
        redisTemplate.delete(redisKey);
    }
}
