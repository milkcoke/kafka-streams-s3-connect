package bypass.repository

import aws.sdk.kotlin.services.s3.S3Client
import aws.sdk.kotlin.services.s3.model.GetObjectRequest
import aws.smithy.kotlin.runtime.content.toByteArray

// TODO: what's suspend keyword
class S3RepositoryImpl(
  private val s3Client: S3Client
) : S3Repository {
  override suspend fun findByBucketPath(bucketName: String, objectPath: String): ByteArray {
    val request = GetObjectRequest {
      bucket = bucketName
      key = objectPath
    }
    return s3Client.use { s3 ->
      s3.getObject(request) {response ->
        response.body!!.toByteArray()
      }
    }

  }
}
