package com.dlywlotus.echo_backend.constants;

public class StompConstants {
    public static final String ROOM_PREFIX = "/topic/room/";
    public static final String USER_PREFIX = "/queue/user/";
    public static final String NEW_ROOM_POSTFIX = "/new-room";
    public static final String ACTIVE_USERS_TOPIC = "/topic/global/stats/active-users";

    public static String getUserNewRoomTopic(String userId) {
        return USER_PREFIX + userId + NEW_ROOM_POSTFIX;
    }
}
