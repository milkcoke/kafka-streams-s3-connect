package bypass.repository

import aws.sdk.kotlin.services.s3.S3Client
import aws.sdk.kotlin.services.s3.model.NoSuchKey
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test

class S3RepositoryImplTest {
  private val s3Repository: S3Repository = S3RepositoryImpl(S3Client{
    region = "ap-northeast-2"
  })

  @Test
  fun findByBucketPath() = runTest {
    val byteArray : ByteArray = s3Repository.findByBucketPath(
      "milkcoke-logs",
      "original.log"
    )

    println(byteArray.toString(Charsets.UTF_8))
  }

  @Test
  fun failedToFindByBucketPath() = runTest {
     assertThatThrownBy {
       runBlocking {
         s3Repository.findByBucketPath(
           "milkcoke-logs",
           "invalid-path"
         )
       }
     }.isInstanceOf(NoSuchKey::class.java)
  }
}
