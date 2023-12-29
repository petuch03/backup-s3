package petuch03.backups3

import java.io.File
import java.io.FileInputStream
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

class ZipManager {

    fun zipDirectory(directoryPath: String): String {
        val sourceDirPath = Paths.get(directoryPath)
        val zipFilePath = "$directoryPath.zip"
        ZipOutputStream(Files.newOutputStream(Paths.get(zipFilePath))).use { zos ->
            Files.walk(sourceDirPath).filter { path -> !Files.isDirectory(path) }.forEach { path ->
                val zipEntry = ZipEntry(sourceDirPath.relativize(path).toString())
                zos.putNextEntry(zipEntry)
                Files.copy(path, zos)
                zos.closeEntry()
            }
        }
        return zipFilePath
    }

    fun extractZip(zipFilePath: String, targetPath: String) {
        File(zipFilePath).deleteOnExit()
        ZipInputStream(FileInputStream(zipFilePath)).use { zis ->
            var zipEntry = zis.nextEntry
            while (zipEntry != null) {
                val newFile = File(targetPath, zipEntry.name)
                if (zipEntry.isDirectory) {
                    newFile.mkdirs()
                } else {
                    newFile.parentFile.mkdirs()
                    Files.copy(zis, newFile.toPath(), StandardCopyOption.REPLACE_EXISTING)
                }
                zipEntry = zis.nextEntry
            }
        }
    }

    fun extractFileFromZip(zipFilePath: String, fileName: String, targetPath: String) {
        File(zipFilePath).deleteOnExit()
        ZipInputStream(FileInputStream(zipFilePath)).use { zis ->
            var zipEntry = zis.nextEntry
            while (zipEntry != null) {
                if (zipEntry.name == fileName) {
                    val newFile = File(targetPath, zipEntry.name)
                    newFile.parentFile.mkdirs()
                    Files.copy(zis, newFile.toPath(), StandardCopyOption.REPLACE_EXISTING)
                    println("File '$fileName' extracted to '$targetPath'")
                    return
                }
                zipEntry = zis.nextEntry
            }
            println("File '$fileName' not found in the zip.")
        }
    }
}
