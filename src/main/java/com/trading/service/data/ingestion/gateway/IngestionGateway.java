package com.trading.service.data.ingestion.gateway;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.trading.service.data.ingestion.model.BarEvent;
import com.trading.service.data.ingestion.model.MarketDataEvent;
import com.trading.service.data.ingestion.service.MarketDataIngestionService;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicLong;

/** Parses raw inbound JSON messages into normalized market data events and routes them. */
public class IngestionGateway {
  private final MarketDataIngestionService service;
  private final ObjectMapper mapper = new ObjectMapper();
  private final AtomicLong parsed = new AtomicLong();
  private final AtomicLong dropped = new AtomicLong();

  public IngestionGateway(MarketDataIngestionService service) {
    this.service = service;
  }

  public void onRaw(String payload) {
    if (payload == null || payload.isEmpty()) {
      dropped.incrementAndGet();
      return;
    }
    try {
      JsonNode root = mapper.readTree(payload);
      if (isBar(root)) {
        BarEvent b = toBar(root);
        if (b != null) {
          service.submitBar(b);
          parsed.incrementAndGet();
        } else dropped.incrementAndGet();
      } else if (isTick(root)) {
        MarketDataEvent t = toTick(root);
        if (t != null) {
          service.submitTick(t);
          parsed.incrementAndGet();
        } else dropped.incrementAndGet();
      } else {
        dropped.incrementAndGet();
      }
    } catch (Exception e) {
      dropped.incrementAndGet();
    }
  }

  private boolean isTick(JsonNode n) {
    return n.has("bid") || n.has("ask") || n.has("last");
  }

  private boolean isBar(JsonNode n) {
    return n.has("open") && n.has("high") && n.has("low") && n.has("close");
  }

  private MarketDataEvent toTick(JsonNode n) {
    if (!n.hasNonNull("symbol")) return null;
    long ts = n.has("ts") ? n.get("ts").asLong() : Instant.now().toEpochMilli();
    double bid = n.path("bid").asDouble(Double.NaN);
    double ask = n.path("ask").asDouble(Double.NaN);
    double last = n.path("last").asDouble(Double.NaN);
    double vol = n.path("volume").asDouble(Double.NaN);
    return new MarketDataEvent(n.get("symbol").asText(), ts, bid, ask, last, vol);
  }

  private BarEvent toBar(JsonNode n) {
    if (!n.hasNonNull("symbol")) return null;
    String sym = n.get("symbol").asText();
    String interval = n.path("interval").asText("1m");
    long start = n.path("start").asLong(n.path("ts").asLong(Instant.now().toEpochMilli()));
    long end = n.path("end").asLong(start + 60_000);
    double open = n.get("open").asDouble();
    double high = n.get("high").asDouble();
    double low = n.get("low").asDouble();
    double close = n.get("close").asDouble();
    double volume = n.path("volume").asDouble(Double.NaN);
    return new BarEvent(sym, interval, start, end, open, high, low, close, volume);
  }

  public long getParsedCount() {
    return parsed.get();
  }

  public long getDroppedCount() {
    return dropped.get();
  }
}
