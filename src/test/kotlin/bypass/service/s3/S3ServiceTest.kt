package bypass.service.s3

import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class S3ServiceTest {
  private val s3Service = S3Service()
  @Test
  fun testGetObjectContentAsLines() = runTest {
    val s3Path = """
            {
                "bucket": "milkcoke-logs",
                "path": "/original.log"
            }
        """.trimIndent()
    val lines = s3Service.getObjectContentAsLines(s3Path)
    lines.forEach { println(it) }
  }
}
