package com.dlywlotus.echo_backend.constants;

public class RedisConstants {
    // Note: KEY refers to the redis key, the outer key, while HASH_KEY refers to the inner key inside a redis hash

    // Redis keys and key prefixes
    public static final String LOBBY_KEY = "list:waiting_sessions";
    public static final String ROOM_KEY_PREFIX = "room:";

    // Redis hash keys and hash key prefixes
    public static final String USER_NAME_HASH_KEY = "username";
    public static final String USER_ID_HASH_KEY = "userId";

    public static String getRoomRedisKey(String roomId) {
        return ROOM_KEY_PREFIX + roomId;
    }
}
