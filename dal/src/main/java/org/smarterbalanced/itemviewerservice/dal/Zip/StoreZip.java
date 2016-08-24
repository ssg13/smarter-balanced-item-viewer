package org.smarterbalanced.itemviewerservice.dal.Zip;

import static org.apache.commons.io.IOUtils.toByteArray;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;


/**
 * Class with methods for storing files in Redis and AWS S3 buckets.
 */
public class StoreZip {
  private static final Logger logger = LoggerFactory.getLogger(StoreZip.class);

  /**
   * Extract zip.
   *
   * @param zipFilePath        path to the zip file that needs to be extracted
   * @param extractionLocation directory to extract the zip file into
   */
  public static void extractZip(String zipFilePath, String extractionLocation) {
    Path filePath;
    byte[] fileData;
    ZipEntry entry;
    ZipFile zipFile = null;
    try {
      zipFile = new ZipFile(zipFilePath);
      Enumeration<? extends ZipEntry> entries = zipFile.entries();

      while (entries.hasMoreElements()) {
        entry = entries.nextElement();
        if (!entry.isDirectory()) {
          InputStream zipStream = zipFile.getInputStream(entry);
          fileData = toByteArray(zipStream);
          filePath = Paths.get(extractionLocation + "/" + entry.getName());
          Files.createDirectories(filePath.getParent());
          Files.write(filePath, fileData);
        }
      }
      zipFile.close();
    } catch (IOException e) {
      logger.error("Unable to extract zip file to location: " + extractionLocation, e);
    }
  }

}
