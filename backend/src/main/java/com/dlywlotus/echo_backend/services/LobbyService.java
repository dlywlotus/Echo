package com.dlywlotus.echo_backend.services;

import com.dlywlotus.echo_backend.constants.RedisConstants;
import com.dlywlotus.echo_backend.constants.StompConstants;
import com.dlywlotus.echo_backend.dtos.RoomDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class LobbyService {
    private final RedisTemplate<String, String> redisTemplate;
    private final SimpMessagingTemplate stompTemplate;

    public void processQueue() {
        if (getQueueSize() < 2) return;
        log.info(">>>>>>>>>>>>>>>>>>>> PROCESSING QUEUE");

        String userOneKey = popFromQueue();
        String userTwoKey = popFromQueue();
        HashOperations<String, String, String> hashOps = redisTemplate.opsForHash();

        //If the user B disconnected, add user A back to the front of the queue
        if (!redisTemplate.hasKey(userOneKey) && !redisTemplate.hasKey(userTwoKey)) {
            return;
        } else if (!redisTemplate.hasKey(userOneKey)) {
            String userTwoName = hashOps.get(userTwoKey, RedisConstants.USER_NAME_HASH_KEY);
            joinQueue(userTwoKey, userTwoName);
            return;
        } else if (!redisTemplate.hasKey(userTwoKey)) {
            String userOneName = hashOps.get(userOneKey, RedisConstants.USER_NAME_HASH_KEY);
            joinQueue(userOneKey, userOneName);
            return;
        }
        String userOneId = hashOps.get(userOneKey, RedisConstants.USER_ID_HASH_KEY);
        String userTwoId = hashOps.get(userTwoKey, RedisConstants.USER_ID_HASH_KEY);

        //Create a chat room with the details of both users
        RoomDetails chatRoom = new RoomDetails(UUID.randomUUID(),
                userOneId, hashOps.get(userOneKey, RedisConstants.USER_NAME_HASH_KEY),
                userTwoId, hashOps.get(userTwoKey, RedisConstants.USER_NAME_HASH_KEY)
        );

        //Send message to both users
        stompTemplate.convertAndSend(StompConstants.getUserNewRoomTopic(userOneId), chatRoom);
        stompTemplate.convertAndSend(StompConstants.getUserNewRoomTopic(userTwoId), chatRoom);
    }

    public void joinQueue(String redisKey, String username) {
        // Add session to the lobby
        redisTemplate.opsForList().rightPush(RedisConstants.LOBBY_KEY, redisKey);
        // Set username for session
        redisTemplate.opsForHash().put(redisKey, RedisConstants.USER_NAME_HASH_KEY, username);
    }

    public String popFromQueue() {
        return redisTemplate.opsForList().leftPop(RedisConstants.LOBBY_KEY);
    }

    public long getQueueSize() {
        return redisTemplate.opsForList().size(RedisConstants.LOBBY_KEY);
    }

}
