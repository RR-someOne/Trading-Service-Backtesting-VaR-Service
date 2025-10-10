package com.trading.service.api;

import io.javalin.Javalin;
import io.javalin.http.Context;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class RestApiServer implements AutoCloseable {
  private final Javalin app;
  private final AtomicBoolean running = new AtomicBoolean(false);
  private final String apiKey;
  private final ControlHandler handler;

  public RestApiServer(int port, String apiKey) {
    this(port, apiKey, new ControlHandler() {
      @Override
      public void startIngestion() {}

      @Override
      public void stopIngestion() {}

      @Override
      public java.util.Map<String, Object> status() {
        return java.util.Map.of("status", "ok");
      }
    });
  }

  public RestApiServer(int port, String apiKey, ControlHandler handler) {
    this.apiKey = apiKey;
    this.handler = handler;
    this.app = Javalin.create(config -> config.showJavalinBanner = false);
    registerRoutes();
    this.app.start(port);
    running.set(true);
  }

  private void registerRoutes() {
    app.before(this::authn);
    app.get("/status", ctx -> ctx.json(handler.status()));
    app.post(
        "/control/:action",
        ctx -> {
          String action = ctx.pathParam("action");
          switch (action) {
            case "start-ingestion":
              try {
                handler.startIngestion();
                ctx.json(Map.of("action", action, "result", "accepted"));
              } catch (Exception e) {
                ctx.status(500).json(Map.of("error", e.getMessage()));
              }
              break;
            case "stop-ingestion":
              try {
                handler.stopIngestion();
                ctx.json(Map.of("action", action, "result", "accepted"));
              } catch (Exception e) {
                ctx.status(500).json(Map.of("error", e.getMessage()));
              }
              break;
            default:
              ctx.status(400).json(Map.of("error", "unknown action"));
          }
        });
  }

  private void authn(Context ctx) {
    if (apiKey == null || apiKey.isEmpty()) return; // allow if not set
    String provided = ctx.header("x-api-key");
    if (provided == null || !provided.equals(apiKey)) {
      ctx.status(401).result("Unauthorized");
    }
  }

  @Override
  public void close() {
    if (running.compareAndSet(true, false)) {
      app.stop();
    }
  }
}
