# Echo

Connect with other users and chat anonymously

## Tech stack

### Front end

- NextJs
- Tailwind
- ShadCn

### Back end

- Spring boot
- Redis
- StompJs

### Local development

- Start containers: `docker compose up -d`
- Stop containers: `docker compose down -v`

### Integration tests

#### Backend integration tests

The integration tests require fresh docker containers. Make sure to run `docker compose down -v` and
then
`docker compose up -d` before running `mvn clean verify` from the backend folder.

#### Front end implementation requirements set by backend

- When the user establishes the web socket connection it has to send a generated uuid in the
  connection
  header as `"user-id" = <GENERATED_UUID>`
- When the user receives the "new room" event it should query the `"/room/{roomId}/validate"` web
  socket
  route to see if the room still has 2 participants