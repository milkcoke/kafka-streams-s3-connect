package sourceconnector.config;

import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;

@Configuration
public class ProducerConfig {
  @Bean
  public Properties produerProperties(KafkaProperties kafkaProperties) {
    Properties properties = new Properties();
    properties.putAll(kafkaProperties.getProducer().buildProperties(null));
    return properties;
  }
}
