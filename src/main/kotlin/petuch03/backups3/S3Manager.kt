package petuch03.backups3

import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.model.GetObjectRequest
import com.amazonaws.services.s3.model.ListObjectsV2Request
import java.io.File
import java.nio.file.Paths
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

class S3Manager(private val s3client: AmazonS3) {
    fun uploadToS3(bucketName: String, targetPath: String, filePath: String) {
        val file = File(filePath)
        val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmm"))
        val fileNameWithTimestamp = insertTimestampBeforeExtension(file.name, timestamp)
        val key = "$targetPath/$fileNameWithTimestamp"
        s3client.putObject(bucketName, key, file)
        println("File uploaded to S3: $key")
        file.deleteOnExit()
    }

    fun downloadFromS3(bucketName: String, remotePath: String, localPath: String): String {
        val fileName = Paths.get(remotePath).fileName.toString()
        val localFilePath = "$localPath/$fileName"
        val file = File(localFilePath)

        s3client.getObject(GetObjectRequest(bucketName, remotePath), file)
        return file.absolutePath
    }

    fun findLatestBackup(bucketName: String, path: String): String {
        val backups = listBackupsInPath(bucketName, path)
        return backups.maxByOrNull { parseTimestampFromKey(it) } ?: throw IllegalStateException("No backups found in the specified path")
    }

    fun findBackupByDateTime(bucketName: String, path: String, dateTime: String): String {
        val formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmm")
        val desiredDateTime = try {
            LocalDateTime.parse(dateTime, formatter)
        } catch (e: DateTimeParseException) {
            throw IllegalArgumentException("Invalid date-time format. Expected format: yyyyMMdd_HHmm")
        }

        val backups = listBackupsInPath(bucketName, path)
        val matchedBackup = backups.firstOrNull { backup ->
            val backupDateTimeString = backup.substringAfterLast("-").substringBeforeLast(".")
            try {
                val backupDateTime = LocalDateTime.parse(backupDateTimeString, formatter)
                backupDateTime.isEqual(desiredDateTime)
            } catch (e: DateTimeParseException) {
                false
            }
        }

        return matchedBackup ?: throw IllegalStateException("No backup found for the specified date and time")
    }

    private fun insertTimestampBeforeExtension(fileName: String, timestamp: String): String {
        val dotIndex = fileName.lastIndexOf('.')
        return if (dotIndex != -1) {
            fileName.substring(0, dotIndex) + "-" + timestamp + fileName.substring(dotIndex)
        } else {
            "$fileName-$timestamp"
        }
    }

    private fun listBackupsInPath(bucket: String, path: String): List<String> {
        val request = ListObjectsV2Request().apply {
            bucketName = bucket
            prefix = path
        }
        val objectListing = s3client.listObjectsV2(request)
        return objectListing.objectSummaries.map { it.key }
    }

    private fun parseTimestampFromKey(key: String): LocalDateTime {
        val pattern = DateTimeFormatter.ofPattern("yyyyMMdd_HHmm")
        val timestampPart = key.substringAfterLast("-").substringBeforeLast(".")
        return LocalDateTime.parse(timestampPart, pattern)
    }
}