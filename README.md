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

The integration tests require fresh docker containers. Make sure to run `docker compose down -v` and then
`docker compose up -d` before running `mvn clean verify` from the backend folder.