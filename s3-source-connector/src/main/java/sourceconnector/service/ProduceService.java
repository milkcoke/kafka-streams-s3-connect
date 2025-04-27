package sourceconnector.service;

import lombok.extern.slf4j.Slf4j;
import sourceconnector.domain.BatchMessages;
import sourceconnector.domain.OffsetRecord;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Properties;

@Slf4j
@RequiredArgsConstructor
public class ProduceService {
  private final String logTopic;
  private final String offsetTopic;
  private final KafkaProducer<String, byte[]> kafkaProducer;

  public ProduceService(Properties properties,
                        String logTopic,
                        String offsetTopic) {
    this.kafkaProducer = new KafkaProducer<>(properties);
    this.logTopic = logTopic;
    this.offsetTopic = offsetTopic;
  }

  public void send(
    OffsetRecord offsetRecord,
    BatchMessages batchMessages
  ) {
    List<String> messages = batchMessages.get();

    this.kafkaProducer.initTransactions();
    try {
      this.kafkaProducer.beginTransaction();

      for (String message : messages) {
        this.kafkaProducer.send(new ProducerRecord<>(
          logTopic,
          null,
          message.getBytes())
        );
      }
      this.kafkaProducer.send(new ProducerRecord<>(
        this.offsetTopic,
        offsetRecord.key(),
        ByteBuffer
          .allocate(Long.BYTES)
          .putLong(offsetRecord.offset())
          .array()
      ));

      this.kafkaProducer.commitTransaction();
    } catch (Exception e) {
      log.error("Abort transaction since {}", e.getMessage());
      this.kafkaProducer.abortTransaction();
    }

  }

}
