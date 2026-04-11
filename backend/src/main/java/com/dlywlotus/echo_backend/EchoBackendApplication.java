package com.dlywlotus.echo_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class EchoBackendApplication {
    //TODO: Integration tests
    // 2. Test if system can handle user spamming join room button (assuming it is not handled on FE)

    public static void main(String[] args) {
        SpringApplication.run(EchoBackendApplication.class, args);
    }
}
