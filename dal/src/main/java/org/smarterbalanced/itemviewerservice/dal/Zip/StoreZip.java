package org.smarterbalanced.itemviewerservice.dal.Zip;

import static com.amazonaws.util.IOUtils.toByteArray;

import org.smarterbalanced.itemviewerservice.dal.AmazonApi.AmazonFileApi;
import org.smarterbalanced.itemviewerservice.dal.Exceptions.FileTooLargeException;
import org.smarterbalanced.itemviewerservice.dal.Exceptions.RedisFileException;
import org.smarterbalanced.itemviewerservice.dal.Redis.RedisConnection;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;


/**
 * Class with methods for storing files in Redis and AWS S3 buckets.
 */
public class StoreZip {
  private static final Logger log = Logger.getLogger("org.smarterbalanced.dal");
  /**
   * Unpack to bucket string.
   *
   * @param fileStream File stream connected to a zip file.
   * @param fileName   Name of the file. Will be used to create an AWS bucket,
   *                   or upload to an existing one.
   * @return The name of the Amazon bucket the files are stored in.
   */
  public static String unpackToBucket(InputStream fileStream, String fileName) throws IOException {
    String bucketName = "sb-" + fileName;
    ZipInputStream zipStream = new ZipInputStream(fileStream);
    ZipEntry entry;
    int size;
    AmazonFileApi amazonApi = new AmazonFileApi(bucketName);
    try {
      while ((entry = zipStream.getNextEntry()) != null) {
        size = Math.toIntExact(entry.getSize());
        byte[] fileData = new byte[size];
        zipStream.read(fileData);
        InputStream entryStream = new ByteArrayInputStream(fileData);
        amazonApi.storeFile(entryStream, entry.getName(), entry.getSize());
      }
      zipStream.close();
    } catch (IOException e) {
      log.log(Level.WARNING, "Failed to store file: " + fileName + " in Amazon bucket");
      throw e;
    }
    return bucketName;
  }

  /**
   * Extracts and stores the contents of a zip file in Redis.
   *
   * @param path File file path for a zip file to unpack.
   * @param redis      The Redis instance to unpack the zip to.
   */
  public static void unpackToRedis(String path, RedisConnection redis)
          throws IOException, FileTooLargeException, RedisFileException {
    ZipFile zipFile = new ZipFile(path);
    ZipEntry entry;
    int size;
    try {
      Enumeration<? extends ZipEntry> entries = zipFile.entries();

      while (entries.hasMoreElements()) {
        entry = entries.nextElement();
        if (!entry.isDirectory()) {
          size = Math.toIntExact(entry.getSize());
          InputStream zipStream = zipFile.getInputStream(entry);
          byte[] fileData = toByteArray(zipStream);
          redis.storeByteFile(entry.getName(), fileData);
        }
      }
    } catch (IOException e) {
      log.log(Level.SEVERE, "Error opening Zip file.", e);
      throw e;
    } catch (FileTooLargeException e) {
      log.log(Level.SEVERE, "File too large to store in Redis.", e);
      throw e;
    } catch (RedisFileException e) {
      log.log(Level.SEVERE, "Failed to store file in Redis.", e);
      throw e;
    }
  }

}
