package com.dlywlotus.echo_backend.Exceptions;

public class WebSocketException extends RuntimeException {
    public WebSocketException(String message) {
        super(message);
    }
}
