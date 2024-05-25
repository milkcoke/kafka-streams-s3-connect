package bypass.repository

interface S3Repository {
  suspend fun findByBucketPath(bucketName: String, objectPath: String): ByteArray
}
