package com.dlywlotus.echo_backend.controllers;

import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.web.bind.annotation.RestController;

import com.dlywlotus.echo_backend.Exceptions.WebSocketException;
import com.dlywlotus.echo_backend.dtos.JoinRoomRequest;
import com.dlywlotus.echo_backend.dtos.SendMessageRequest;
import com.dlywlotus.echo_backend.dtos.SendTypingRequest;
import com.dlywlotus.echo_backend.services.ChatRoomService;
import com.dlywlotus.echo_backend.services.LobbyService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class WebSocketController {
    private final ChatRoomService chatRoomService;
    private final LobbyService lobbyService;

    @MessageMapping("/lobby/join")
    public void joinLobby(@Payload JoinRoomRequest request,
                          SimpMessageHeaderAccessor simpMessageHeaderAccessor) {
        String redisKey = getRedisKey(simpMessageHeaderAccessor);
        lobbyService.joinQueue(redisKey, request.username());
    }

    @MessageMapping("/room/{roomId}/leave")
    public void leaveRoom(@DestinationVariable String roomId,
                          SimpMessageHeaderAccessor simpMessageHeaderAccessor) {
        String redisKey = getRedisKey(simpMessageHeaderAccessor);
        chatRoomService.leaveRoom(redisKey, roomId);
    }

    @MessageMapping("/room/{roomId}/message")
    public void sendMessageEvent(@Payload SendMessageRequest request, @DestinationVariable String roomId,
                                 SimpMessageHeaderAccessor simpMessageHeaderAccessor) {
        String redisKey = getRedisKey(simpMessageHeaderAccessor);
        chatRoomService.sendMessageEvent(redisKey, request.content(), roomId);
    }

    @MessageMapping("/room/{roomId}/typing")
    public void sendTypingEvent(@Payload SendTypingRequest request, @DestinationVariable String roomId,
                                SimpMessageHeaderAccessor simpMessageHeaderAccessor) {
        String redisKey = getRedisKey(simpMessageHeaderAccessor);
        chatRoomService.sendTypingEvent(redisKey, request.isTyping(), roomId);
    }

    // To check if the room is still valid (has two users)
    @MessageMapping("/room/{roomId}/validate")
    public void validateRoom(@DestinationVariable String roomId) {
        chatRoomService.validateRoom(roomId);
    }

    private String getRedisKey(SimpMessageHeaderAccessor simpMessageHeaderAccessor) {
        String sessionId = simpMessageHeaderAccessor.getSessionId();
        if (sessionId == null) {
            throw new WebSocketException("Invalid web socket session id");
        }
        return "session:" + sessionId;
    }
}
