package com.trading.service.data;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.trading.service.net.HttpClientConfig;
import com.trading.service.net.HttpClientFactory;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.http.HttpClient;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class DataServiceTest {
  private com.sun.net.httpserver.HttpServer server;
  private int port;
  private DataService dataService;

  @Before
  public void setUp() throws Exception {
    server = com.sun.net.httpserver.HttpServer.create(new InetSocketAddress(0), 0);
    port = server.getAddress().getPort();
    server.createContext(
        "/ok",
        exchange -> {
          byte[] bytes = "{\"v\":1}".getBytes();
          exchange.getResponseHeaders().add("Content-Type", "application/json");
          exchange.sendResponseHeaders(200, bytes.length);
          try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
          }
        });
    server.createContext(
        "/err",
        exchange -> {
          byte[] bytes = "boom".getBytes();
          exchange.sendResponseHeaders(500, bytes.length);
          try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
          }
        });
    server.start();
    HttpClient client = HttpClientFactory.shared(HttpClientConfig.builder().build());
    dataService = new DataService(client);
  }

  @After
  public void tearDown() {
    server.stop(0);
  }

  @Test
  public void fetchJson_ok() throws Exception {
    String body = dataService.fetchJson("http://localhost:" + port + "/ok");
    assertTrue(body.contains("\"v\":1"));
  }

  @Test
  public void fetchJson_error() throws Exception {
    try {
      dataService.fetchJson("http://localhost:" + port + "/err");
      fail("Expected exception");
    } catch (Exception e) {
      assertTrue(e.getMessage().contains("HTTP status"));
    }
  }
}
