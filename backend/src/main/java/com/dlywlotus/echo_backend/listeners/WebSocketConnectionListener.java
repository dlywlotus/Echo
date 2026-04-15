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
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketConnectionListener {
    private final RedisTemplate<String, String> redisTemplate;
    private final SimpMessagingTemplate stompTemplate;

    @EventListener
    public void handleSessionConnect(SessionConnectEvent event) {
        log.info(">>>>>>>>>>>>>>>>>>>> SESSION CONNECT");
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();

        if (sessionId == null) return;

        // Extract User ID from the headers sent by the Frontend
        // Note: "user-id" must match key sent in "connectHeaders"
        String userId = headerAccessor.getFirstNativeHeader("user-id");

        // Set the userId in the redis hash
        String sessionKey = RedisConstants.SESSION_KEY_PREFIX + sessionId;
        redisTemplate.opsForHash().put(sessionKey, RedisConstants.USER_ID_HASH_KEY, userId);

        // Set a TTL of 1 day in case the server crashes and session disconnect event doesn't fire
        redisTemplate.expire(sessionKey, 1, TimeUnit.DAYS);
    }

    @EventListener
    public void handleSessionDisconnect(SessionDisconnectEvent event) {
        log.info(">>>>>>>>>>>>>>>>>>>> SESSION DISCONNECT");
        String sessionKey = RedisConstants.SESSION_KEY_PREFIX + event.getSessionId();

        // Remove session from lobby list and lobby set if the user was in it
        redisTemplate.opsForList().remove(RedisConstants.LOBBY_LIST_KEY, 1, sessionKey);
        redisTemplate.opsForSet().remove(RedisConstants.LOBBY_SET_KEY, sessionKey);

        HashOperations<String, String, String> hashOperations = redisTemplate.opsForHash();
        String roomId = hashOperations.get(sessionKey, RedisConstants.ROOM_ID_HASH_KEY);
        if (!Objects.isNull(roomId)) {
            // Send "DISCONNECT" event to the room topic to notify the other user
            ChatRoomEvent roomEvent = new ChatRoomEvent(RoomEventType.DISCONNECT, null, null);
            stompTemplate.convertAndSend(StompConstants.ROOM_PREFIX + roomId, roomEvent);

            // Remove user session from room redis set
            redisTemplate.opsForSet().remove(RedisConstants.getRoomRedisKey(roomId), sessionKey);
        }

        //Remove session from redis
        redisTemplate.delete(sessionKey);
    }
}
