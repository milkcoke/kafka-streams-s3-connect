package sourceconnector.domain;

import org.apache.kafka.clients.producer.ProducerRecord;

public interface OffsetRecord {
  String key();
  long offset();
}
