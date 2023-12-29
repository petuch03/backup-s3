# S3 Backup and Restore Tool

## Overview

This tool is a Kotlin-based application designed to perform backup and restore operations with Amazon S3. 
It allows users to compress and upload directories to an S3 bucket and restore them, either completely or partially, from the backup.

## Features

- **Backup**: Compress and upload a specified directory to an AWS S3 bucket.
- **Restore**: Download and extract backups from S3. Users can choose to restore the latest backup by default or specify a particular backup to restore.
- **File Specific Restore**: Ability to restore an individual file from a backup.
- **Timestamps in Backup**: Backups are timestamped for easy identification and organization.

## Configuration
Before using the tool, configure the following settings in a `.env` file in the project's root directory:

```
AWS_ACCESS_KEY=your_access_key
AWS_SECRET_KEY=your_secret_key
AWS_REGION=your_region
S3_BUCKET_NAME=your_bucket_name
```

## Usage

### Backup

To create a backup:

```bash
java -jar s3-backup-tool.jar -m backup -l [local directory to backup] -r [remote S3 path] 
```
This will compress the specified directory and upload it to the given S3 path.

### Restore
To restore a backup:
```bash
java -jar s3-backup-tool.jar -m restore -r [remote S3 path] -l [local target path] -bk [optional specific backup key]
```
This will restore the latest backup to the specified local path by default. 
If a specific backup key is provided, it will restore that particular backup.

### Restore a Specific File
To restore a specific file from a backup:
```bash
java -jar s3-backup-tool.jar -m restore -r [remote S3 path] -l [local target path] -rf [specific file to restore] -bk [optional specific backup key]
```
This command restores a specified file from the chosen backup.

### Dependencies

* AWS SDK for Java
* kotlinx-cli for command-line argument parsing
* dotenv-java for environment variable management

### Building

This is a Kotlin-based application. Use Gradle to compile and build the executable JAR:
```bash
gradle jar
```

### Example

```bash
es@M1-ES-7:backup-s3$ java -jar build/libs/app.jar -m backup -l src -r backup-tool/test-src-backup
File uploaded to S3: backup-tool/test-src-backup/src-20231229_1603.zip

es@M1-ES-7:backup-s3$ java -jar build/libs/app.jar -m restore -l src-backup -r backup-tool/test-src-backup
Restored backup backup-tool/test-src-backup/src-20231229_1603.zip into src-backup

es@M1-ES-7:backup-s3$ java -jar build/libs/app.jar -m restore -l src-backup -r backup-tool/test-src-backup -bk 20231229_1556
Restored backup backup-tool/test-src-backup/src-20231229_1556.zip into src-backup

es@M1-ES-7:backup-s3$ java -jar build/libs/app.jar -m restore -l src-backup -r backup-tool/test-src-backup -rf main/kotlin/petuch03/backups3/App.kt
File 'main/kotlin/petuch03/backups3/App.kt' extracted to 'src-backup'
Restored backup backup-tool/test-src-backup/src-20231229_1603.zip into src-backup

es@M1-ES-7:backup-s3$ java -jar build/libs/app.jar -m restore -l src-backup -r backup-tool/test-src-backup -rf main/kotlin/petuch03/backups3/App.kt -bk 20231229_1556
File 'main/kotlin/petuch03/backups3/App.kt' extracted to 'src-backup'
Restored backup backup-tool/test-src-backup/src-20231229_1556.zip into src-backup

es@M1-ES-7:backup-s3$ 
```
