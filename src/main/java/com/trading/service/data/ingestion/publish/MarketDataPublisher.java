package com.trading.service.data.ingestion.publish;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.trading.service.data.ingestion.config.IngestionConfig;
import com.trading.service.data.ingestion.model.BarEvent;
import com.trading.service.data.ingestion.model.MarketDataEvent;
import java.util.Properties;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

/** Kafka publisher wrapper. */
public class MarketDataPublisher implements Publisher {
  private final IngestionConfig config;
  private final KafkaProducer<String, String> producer;
  private final ObjectMapper mapper = new ObjectMapper();

  public MarketDataPublisher(IngestionConfig config) {
    this.config = config;
    Properties p = new Properties();
    p.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, config.getKafkaBootstrapServers());
    p.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
    p.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
    p.put(ProducerConfig.ACKS_CONFIG, "1");
    p.put(ProducerConfig.LINGER_MS_CONFIG, "5");
    this.producer = new KafkaProducer<>(p);
  }

  @Override
  public void publishTick(MarketDataEvent evt) {
    send(config.getTickTopic(), evt.symbol(), toJson(evt));
  }

  @Override
  public void publishBar(BarEvent evt) {
    send(config.getBarTopic(), evt.symbol(), toJson(evt));
  }

  private void send(String topic, String key, String value) {
    producer.send(new ProducerRecord<>(topic, key, value));
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
