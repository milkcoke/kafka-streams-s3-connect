package sourceconnector.repository;

import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.LongDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import sourceconnector.domain.OffsetRecord;
import sourceconnector.domain.OffsetStatus;

import java.util.List;
import java.util.Map;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;

class S3OffsetRepositoryTest {
  private static S3OffsetRepository repository;

  @BeforeAll
  static void setup() {
    Properties adminProps = new Properties();
    adminProps.put(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, "localhost:9093");

    AdminClient adminClient = AdminClient.create(adminProps);

    Properties consumerProps = new Properties();
    consumerProps.putAll(Map.of(
      CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, "localhost:9093",
      ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class,
      ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, LongDeserializer.class,
      ConsumerConfig.MAX_PARTITION_FETCH_BYTES_CONFIG, 57_671_680, // 55MB
      ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 50_000
    ));
    Consumer<String, Long> consumer = new KafkaConsumer<>(consumerProps);

    repository = new S3OffsetRepository(consumer, 2000, adminClient);
  }

  @DisplayName("Should get INITIAL Offset when not processed key ")
  @Test
  void getLastOffsetRecord() {
     OffsetRecord lastOffset = repository.getLastOffsetRecord(
      "s3-offset-topic",
      "s3://test/2025/04/13/test.txt"
    );

    assertThat(lastOffset)
      .extracting(OffsetRecord::key, OffsetRecord::offset)
      .containsExactly(
        "s3://test/2025/04/13/test.txt",
        OffsetStatus.INITIAL_OFFSET.getValue()
      );
  }

  @DisplayName("Should always get same partition when same key is input")
  @Test
  void getPartitionsForTopic() {
    // given
    String offsetTopic = "s3-offset-topic";

    int partition1 = repository.getPartitionsForTopic(offsetTopic, "s3://test/2025/04/13/test.txt");
    int partition2 = repository.getPartitionsForTopic(offsetTopic, "s3://test/2025/04/13/test.txt");
    int partition3 = repository.getPartitionsForTopic(offsetTopic, "s3://test/2025/04/13/test.txt");

    assertThat(List.of(partition1, partition2, partition3))
      .containsOnly(partition1);
  }
}
