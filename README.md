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

---

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

## Front end implementation requirements set by backend

- When the user establishes the web socket connection it has to send a generated uuid in the
  connection
  header as `"user-id" = <GENERATED_UUID>`
- When the user receives the "new room" event it should query the `"/room/{roomId}/validate"` web
  socket
  route to see if the room still has 2 participants