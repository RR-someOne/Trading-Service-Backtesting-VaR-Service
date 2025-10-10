package com.trading.service.api;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

public class GrpcApiServer implements AutoCloseable {
  private final Server server;
  private final AtomicBoolean running = new AtomicBoolean(false);

  public GrpcApiServer(int port) {
    this.server = ServerBuilder.forPort(port).build();
  }

  public void start() throws IOException {
    server.start();
    running.set(true);
  }

  @Override
  public void close() {
    if (running.compareAndSet(true, false)) {
      server.shutdown();
    }
  }
}
