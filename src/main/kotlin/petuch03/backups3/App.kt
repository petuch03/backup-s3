package petuch03.backups3

import com.amazonaws.auth.AWSStaticCredentialsProvider
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.regions.Regions
import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.AmazonS3ClientBuilder
import io.github.cdimascio.dotenv.*
import kotlinx.cli.ArgParser
import kotlinx.cli.ArgType
import kotlinx.cli.required

class App {
    private val dotenv = Dotenv.load()

    private val awsAccessKey =
        dotenv["AWS_ACCESS_KEY"] ?: throw IllegalStateException("AWS_ACCESS_KEY not found in .env")
    private val awsSecretKey =
        dotenv["AWS_SECRET_KEY"] ?: throw IllegalStateException("AWS_SECRET_KEY not found in .env")
    private val awsRegion =
        dotenv["AWS_REGION"] ?: throw IllegalStateException("AWS_REGION not found in .env")
    private val s3BucketName =
        dotenv["S3_BUCKET_NAME"] ?: throw IllegalStateException("S3_BUCKET_NAME not found in .env")

    private val s3Client = createS3Client()

    fun backup(localPath: String, remotePath: String) {
        val zipManager = ZipManager()
        val zipFilePath = zipManager.zipDirectory(localPath)

        val s3Manager = S3Manager(s3Client)
        s3Manager.uploadToS3(s3BucketName, remotePath, zipFilePath)
    }

    fun restore(remotePath: String, localPath: String, restoreFile: String? = null, specificBackup: String? = null) {
        val s3Manager = S3Manager(createS3Client())
        val backupToRestore = specificBackup?.let {
            s3Manager.findBackupByDateTime(s3BucketName, remotePath, it)
        } ?: s3Manager.findLatestBackup(s3BucketName, remotePath)

        val zipFilePath = s3Manager.downloadFromS3(s3BucketName, backupToRestore, localPath)

        val zipManager = ZipManager()
        if (restoreFile == null) {
            zipManager.extractZip(zipFilePath, localPath)
        } else {
            zipManager.extractFileFromZip(zipFilePath, restoreFile, localPath)
        }
        print("Restored backup $backupToRestore into $localPath")
    }

    private fun createS3Client(): AmazonS3 {
        val credentials = BasicAWSCredentials(awsAccessKey, awsSecretKey)
        return AmazonS3ClientBuilder.standard()
            .withCredentials(AWSStaticCredentialsProvider(credentials))
            .withRegion(Regions.fromName(awsRegion))
            .build()
    }
}

fun main(args: Array<String>) {
    val argParser = ArgParser("s3-backup-tool")
    val mode by argParser.option(
        ArgType.Choice(listOf("backup", "restore"), { it }),
        shortName = "m",
        description = "Mode: backup or restore"
    ).required()
    val localPath by argParser.option(
        ArgType.String,
        shortName = "l",
        description = "Path in local system that requires backup or the path where backup should be restored"
    ).required()
    val remotePath by argParser.option(
        ArgType.String,
        shortName = "r",
        description = "Remote path in S3 bucket where to place a backup or from where restore it"
    ).required()
    val restoreFile by argParser.option(
        ArgType.String,
        shortName = "rf",
        description = "Specific file to restore from the backup"
    )
    val specificBackupKey by argParser.option(
        ArgType.String,
        shortName = "bk",
        description = "Date and time of the backup to restore in format yyyyMMdd_HHmm"
    )

    argParser.parse(args)

    val app = App()
    when (mode) {
        "backup" -> {
            app.backup(localPath, remotePath)
        }
        "restore" -> {
            app.restore(remotePath, localPath, restoreFile, specificBackupKey)
        }
    }
}