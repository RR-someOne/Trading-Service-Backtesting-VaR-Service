package com.trading.service.net;

import java.time.Duration;

/** Configuration holder for shared HTTP client settings. */
public final class HttpClientConfig {
  public final int maxConnections;
  public final Duration connectTimeout;
  public final Duration readTimeout;
  public final Duration writeTimeout;
  public final Duration callTimeout;
  public final int maxIdleConnections;
  public final Duration keepAliveDuration;
  public final boolean enableHttp2;

  private HttpClientConfig(Builder b) {
    this.maxConnections = b.maxConnections;
    this.connectTimeout = b.connectTimeout;
    this.readTimeout = b.readTimeout;
    this.writeTimeout = b.writeTimeout;
    this.callTimeout = b.callTimeout;
    this.maxIdleConnections = b.maxIdleConnections;
    this.keepAliveDuration = b.keepAliveDuration;
    this.enableHttp2 = b.enableHttp2;
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder {
    private int maxConnections = 64;
    private Duration connectTimeout = Duration.ofSeconds(5);
    private Duration readTimeout = Duration.ofSeconds(5);
    private Duration writeTimeout = Duration.ofSeconds(5);
    private Duration callTimeout = Duration.ofSeconds(10);
    private int maxIdleConnections = 10;
    private Duration keepAliveDuration = Duration.ofSeconds(60);
    private boolean enableHttp2 = true;

    public Builder maxConnections(int v) {
      this.maxConnections = v;
      return this;
    }

    public Builder connectTimeout(Duration v) {
      this.connectTimeout = v;
      return this;
    }

    public Builder readTimeout(Duration v) {
      this.readTimeout = v;
      return this;
    }

    public Builder writeTimeout(Duration v) {
      this.writeTimeout = v;
      return this;
    }

    public Builder callTimeout(Duration v) {
      this.callTimeout = v;
      return this;
    }

    public Builder maxIdleConnections(int v) {
      this.maxIdleConnections = v;
      return this;
    }

    public Builder keepAliveDuration(Duration v) {
      this.keepAliveDuration = v;
      return this;
    }

    public Builder enableHttp2(boolean v) {
      this.enableHttp2 = v;
      return this;
    }

    public HttpClientConfig build() {
      return new HttpClientConfig(this);
    }
  }
}
