package sourceconnector.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.DescribeTopicsResult;
import org.apache.kafka.clients.admin.TopicDescription;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.KafkaFuture;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.utils.Utils;
import sourceconnector.domain.OffsetRecord;
import sourceconnector.domain.S3OffsetRecord;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ExecutionException;

import static sourceconnector.domain.OffsetStatus.COMPLETE_OFFSET;
import static sourceconnector.domain.OffsetStatus.INITIAL_OFFSET;

@Slf4j
@RequiredArgsConstructor
public class S3OffsetRepository implements OffsetRepository {
  private final Consumer<String, Long> consumer;
  private final int maxPollRecords;
  private final AdminClient adminClient;
  private final Duration timeout = Duration.ofMillis(100);

  @Override
  public OffsetRecord getLastOffsetRecord(String topicName, String s3Path) {
    int partition = this.getPartitionsForTopic(topicName, s3Path);
    TopicPartition topicPartition = new TopicPartition(topicName, partition);
    this.consumer.assign(List.of(topicPartition));
    long startOffset = this.consumer.beginningOffsets(List.of(topicPartition)).get(topicPartition);
    long currentOffset = startOffset;
    long endOffset = this.consumer.endOffsets(List.of(topicPartition)).get(topicPartition);

    OffsetRecord lastOffsetRecord;

    while (currentOffset < endOffset) {
      this.consumer.seek(topicPartition, currentOffset);

       List<ConsumerRecord<String, Long>> recordList = this.consumer
        .poll(timeout)
        .records(topicPartition);

//       var secondList = this.consumer.poll(timeout).records(topicPartition);

       if (recordList.isEmpty()) break;

       long lastOffset = recordList
        .stream()
        .max(Comparator.comparingLong(ConsumerRecord::offset))
        .map(ConsumerRecord::offset)
        .get();

      currentOffset = lastOffset + 1;

      List<ConsumerRecord<String, Long>> records = recordList
        .stream()
        .filter(record -> record.key().equals(s3Path))
        .toList();
      if (records.isEmpty()) continue;

      ConsumerRecord<String, Long> record = records.getLast();
      lastOffsetRecord = new S3OffsetRecord(
         record.key(),
         record.offset()
       );

      if (lastOffsetRecord.offset() == COMPLETE_OFFSET.getValue()) {
        return lastOffsetRecord;
      }

    }

    return new S3OffsetRecord(s3Path, INITIAL_OFFSET.getValue());
  }

  @Override
  public List<OffsetRecord> findOffsetRecords(String topicName, String key) {
    return List.of();
  }

  public int getPartitionsForTopic(String topicName, String s3Key){
    // get partition count of topic
    DescribeTopicsResult result = adminClient.describeTopics(Collections.singletonList(topicName));
    Map<String, KafkaFuture<TopicDescription>> futures = result.topicNameValues();
    try {
      TopicDescription description = futures.get(topicName).get();
      int partitionCount = description.partitions().size();
      return Utils.murmur2(s3Key.getBytes(StandardCharsets.UTF_8)) % partitionCount;
    } catch (ExecutionException | InterruptedException e) {
      log.error("Failed to get partitions for topic {}", topicName, e);
      throw new PartitionNotFoundException(e.getMessage());
    }
  }
}
