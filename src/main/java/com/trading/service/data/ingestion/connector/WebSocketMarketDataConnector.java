package com.trading.service.data.ingestion.connector;

import com.trading.service.data.ingestion.model.BarEvent;
import com.trading.service.data.ingestion.model.MarketDataEvent;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.net.http.WebSocket.Listener;
import java.time.Duration;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

/** Simplified WebSocket connector (illustrative). */
@SuppressWarnings("unused")
public class WebSocketMarketDataConnector extends AbstractReconnectableConnector
    implements RawMessageCapable {
  private final URI uri;
  private final HttpClient client;
  private final AtomicReference<WebSocket> socket = new AtomicReference<>();
  private volatile Consumer<MarketDataEvent> tickHandler = e -> {};
  private volatile Consumer<BarEvent> barHandler = e -> {};
  private volatile Consumer<String> rawConsumer = s -> {};

  public WebSocketMarketDataConnector(URI uri, ScheduledExecutorService sched) {
    super(sched, Duration.ofSeconds(5));
    this.uri = uri;
    this.client = HttpClient.newHttpClient();
  }

  @Override
  protected boolean doConnect() {
    client
        .newWebSocketBuilder()
        .buildAsync(
            uri,
            new Listener() {
              @Override
              public CompletionStage<?> onText(
                  WebSocket webSocket, CharSequence data, boolean last) {
                webSocket.request(1);
                String payload = data.toString();
                rawConsumer.accept(payload);
                // Keep demo tick for now in case no gateway is attached
                tickHandler.accept(
                    new MarketDataEvent("DEMO", System.currentTimeMillis(), 0.0, 0.0, 0.0, 0.0));
                return null;
              }
            })
        .whenComplete(
            (ws, err) -> {
              if (err == null) socket.set(ws);
            });
    return true; // optimistic
  }

  @Override
  protected void doDisconnect() {
    WebSocket ws = socket.getAndSet(null);
    if (ws != null) {
      try {
        ws.sendClose(WebSocket.NORMAL_CLOSURE, "bye").join();
      } catch (Exception ignored) {
      }
    }
  }

  @Override
  protected String connectorName() {
    return "websocket:" + uri;
  }

  @Override
  public void setTickHandler(java.util.function.Consumer<MarketDataEvent> handler) {
    this.tickHandler = handler;
  }

  @Override
  public void setBarHandler(java.util.function.Consumer<BarEvent> handler) {
    this.barHandler = handler;
  }

  @Override
  public void setRawMessageConsumer(Consumer<String> consumer) {
    this.rawConsumer = consumer;
  }
}
