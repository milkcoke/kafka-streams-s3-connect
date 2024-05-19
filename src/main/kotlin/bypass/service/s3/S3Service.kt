package bypass.service.s3

import aws.sdk.kotlin.services.s3.S3Client
import aws.sdk.kotlin.services.s3.model.GetObjectRequest
import aws.smithy.kotlin.runtime.content.toInputStream
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Component
import java.io.BufferedReader
import java.io.InputStreamReader

@Component
class S3Service {
  private val objectMapper = ObjectMapper()

  suspend fun getObjectContentAsLines(s3Path: String): List<String> {
    val bucketName = parseBucketName(s3Path)
    val keyName = parseKeyName(s3Path)

    val request = GetObjectRequest {
      bucket = bucketName
      key = keyName
    }

    val jsonLines = mutableListOf<String>()

    S3Client { region = "ap-northeast-2" }.use { s3 ->
      s3.getObject(request) { response ->
        println(response)
        val br= BufferedReader(InputStreamReader(response.body?.toInputStream()!!))
        br.useLines { lines->
          lines.forEach { line -> jsonLines.add(line) }
        }
      }
    }


    return jsonLines
  }

  private fun parseBucketName(s3Path: String): String {
    return objectMapper.readTree(s3Path).get("bucket").toString()
  }

  private fun parseKeyName(s3Path: String): String {
    return objectMapper.readTree(s3Path).get("path").toString()
  }
}
