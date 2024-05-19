package bypass.service

import bypass.config.KafkaConfig
import bypass.service.s3.S3Service
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.kstream.Consumed
import org.apache.kafka.streams.kstream.KStream
import org.apache.kafka.streams.kstream.Produced
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class ByPassService(
  private val s3Service: S3Service
) {

  @Autowired
  fun buildPipeline(streamsBuilder: StreamsBuilder) {
    val s3Stream : KStream<ByteArray, String> = streamsBuilder.stream(
      KafkaConfig.SOURCE_TOPIC,
      Consumed.with(Serdes.ByteArray(), Serdes.String())
    )

    s3Stream
      // TODO: S3 Get Object and bypassing only (with stream?)
//      .mapValues { s3Path-> s3Service.getObjectContentAsLines(s3Path) }
      .mapValues { s3Path-> s3Path }
      .to(
        KafkaConfig.SINK_TOPIC,
//        Produced.with(Serdes.Integer(), Serdes.Long())
        Produced.with(Serdes.ByteArray(), Serdes.String())
      )
  }



}
