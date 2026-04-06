package com.dlywlotus.echo_backend.dtos;

import java.util.UUID;

public record RoomDetails(UUID roomId, String userOneId, String userOneName, String userTwoId, String useTwoName) {
}
