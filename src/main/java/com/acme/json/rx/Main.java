package com.acme.json.rx;

import io.helidon.common.LogConfig;
import io.helidon.common.reactive.Single;
import io.helidon.webserver.WebServer;

public final class Main {

    private Main() {
    }

    public static void main(final String[] args) {
        startServer();
    }

    static Single<WebServer> startServer() {

        // load logging configuration
        LogConfig.configureRuntime();

        WebServer server = WebServer.builder()
                .routing(r -> r.register(new TestService()))
                .port(8080)
                .backpressureBufferSize(128)
                .build();

        Single<WebServer> webserver = server.start();

        webserver.forSingle(ws -> {
            System.out.println("WEB server is up! http://localhost:" + ws.port());
            ws.whenShutdown().thenRun(() -> System.out.println("WEB server is DOWN. Good bye!"));
        })
        .exceptionallyAccept(t -> {
            System.err.println("Startup failed: " + t.getMessage());
            t.printStackTrace(System.err);
        });

        return webserver;
    }
}
