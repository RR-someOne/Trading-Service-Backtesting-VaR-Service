package com.trading.service.data.bus;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.trading.service.data.ingestion.config.IngestionConfig;
import com.trading.service.data.ingestion.model.MarketDataEvent;
import java.util.Properties;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

/** Kafka implementation of {@link TickEventBus}. */
public class KafkaTickEventBus implements TickEventBus {
  private final KafkaProducer<String, String> producer;
  private final String topic;
  private final ObjectMapper mapper = new ObjectMapper();

  public KafkaTickEventBus(IngestionConfig cfg) {
    this.topic = cfg.getTickTopic();
    Properties p = new Properties();
    p.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, cfg.getKafkaBootstrapServers());
    p.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
    p.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
    p.put(ProducerConfig.ACKS_CONFIG, "1");
    this.producer = new KafkaProducer<>(p);
  }

  @Override
  public void publish(MarketDataEvent event) {
    producer.send(new ProducerRecord<>(topic, event.symbol(), toJson(event)));
  }

  private String toJson(Object o) {
    try {
      return mapper.writeValueAsString(o);
    } catch (JsonProcessingException e) {
      return "{}";
    }
  }

  @Override
  public void close() {
    producer.close();
  }
}
