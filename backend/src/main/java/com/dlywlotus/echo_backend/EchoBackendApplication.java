package com.dlywlotus.echo_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class EchoBackendApplication {
    //TODO: Add integration tests for main components
    // 1. queue processer

    //TODO: Have the FE send pings every 10s, if the RTT of the pings are too long, display that the user has poor internet on
    // the FE.

    public static void main(String[] args) {
        SpringApplication.run(EchoBackendApplication.class, args);
    }

}
