package sourceconnector.service;

import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.StringSerializer;
import sourceconnector.domain.BatchMessages;
import sourceconnector.domain.DefaultBatchLogs;
import sourceconnector.domain.OffsetRecord;
import sourceconnector.domain.S3OffsetRecord;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Properties;


class ProduceServiceTest {
  private static final Properties props = new Properties();
  static {
    props.putAll(Map.of(
        CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092",
        org.apache.kafka.clients.producer.ProducerConfig.ACKS_CONFIG, "-1",
        org.apache.kafka.clients.producer.ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class,
        org.apache.kafka.clients.producer.ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, ByteArraySerializer.class,
        org.apache.kafka.clients.producer.ProducerConfig.LINGER_MS_CONFIG, 100,
        ProducerConfig.TRANSACTIONAL_ID_CONFIG, "test-s3"
      )
    );
  }
  @Test
  void sendTest() {
    // given
    ProduceService produceService = new ProduceService(
      props,
      "log-topic",
      "s3-offset-topic"
    );
    BatchMessages batchMessages = new DefaultBatchLogs(List.of(
      "log1",
      "log2",
      "log3"
    ));
    OffsetRecord offsetRecord = new S3OffsetRecord("s3://test/2025/04/11/test.json", 3L);
    // when
    produceService.send(offsetRecord, batchMessages);


  }
}
