# Echo

Connect with other users and chat anonymously

## Tech Stack

- Front End: NextJs, Tailwind, ShadCn
- Back End: Spring Boot, Redis, StompJs

## Local development

- Start containers: `docker compose up -d`
- Stop containers: `docker compose down -v`

## Integration tests

### Backend integration tests

The integration tests require fresh docker containers. Make sure to run `docker compose down -v` and
then
`docker compose up -d` before running `mvn clean verify` from the backend folder.

## WebSocket API Reference

All real-time communication uses the STOMP protocol. The default application destination prefix is
`/app`.

### Endpoints

| Destination                   | Action                           | Request Payload      |
|:------------------------------|:---------------------------------|:---------------------|
| `/app/lobby/join`             | Join the matchmaking queue       | `JoinRoomRequest`    |
| `/app/room/{roomId}/leave`    | Exit an active chat room         | `N/A`                |
| `/app/room/{roomId}/message`  | Send a text message              | `SendMessageRequest` |
| `/app/room/{roomId}/typing`   | Broadcast typing status          | `SendTypingRequest`  |
| `/app/room/{roomId}/validate` | Check if the room is still valid | `N/A`                |

### Topics

| Name                               | Usage                          | Message Format  |
|:-----------------------------------|:-------------------------------|:----------------|
| `/queue/user/{userId}/new-room`    | To get new room notifications  | `RoomDetails`   |
| `/topic/global/stats/active-users` | To get the no. of active users | `int`           |
| `/topic/room/{roomId}`             | To get chat room events        | `ChatRoomEvent` |

### Payload Schemas

#### JoinRoomRequest

```
{
  "username": "string"
}
```

#### SendMessageRequest

```
{
  "content": "string"
}
```

#### SendTypingRequest

```
{
  "isTyping": boolean
}
```

### RoomDetails

```
{
  "roomId": "",
  "userOneId": "",
  "userOneName": "",
  "userTwoId": "",
  "userTwoName": ""
}
```

### ChatRoomEvent

ChatRoomEvent(RoomEventType type, String userId, String content, Boolean isTyping, Instant timestamp)

```
{
  "type": "",
  "userId": "",
  "content": "",
  "timestamp": ""
}
```

## Front end implementation requirements set by backend

- When the user establishes the web socket connection it has to send a generated uuid in the
  connection
  header as `"user-id" = <GENERATED_UUID>`
- When the user receives the "new room" event it should query the `"app/room/{roomId}/validate"` end point to see if the
  room still has 2 participants
