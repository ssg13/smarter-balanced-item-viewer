package org.smarterbalanced.itemviewerservice.dal.Zip;

import org.smarterbalanced.itemviewerservice.dal.AmazonApi.AmazonFileApi;
import org.smarterbalanced.itemviewerservice.dal.Redis.RedisConnection;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;


/**
 * Class with methods for storing files in Redis and AWS S3 buckets.
 */
public class StoreZip {
  /**
   * Unpack to bucket string.
   *
   * @param fileStream File stream connected to a zip file.
   * @param fileName   Name of the file. Will be used to create an AWS bucket,
   *                   or upload to an existing one.
   * @return The name of the Amazon bucket the files are stored in.
   */
  public static String unpackToBucket(InputStream fileStream, String fileName) {
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
        zipStream.close();
      }
    } catch (Exception e) {
      System.err.println("ERROR: Failed to store file in Amazon bucket");
    }
    return bucketName;
  }

  /**
   * Extracts and stores the contents of a zip file in Redis.
   *
   * @param fileStream File stream connected to a zip file.
   * @param redis      The Redis instance to unpack the zip to.
   */
  public static void unpackToRedis(InputStream fileStream, RedisConnection redis) {
    ZipInputStream zipStream = new ZipInputStream(fileStream);
    ZipEntry entry;
    int size;
    try {
      while ((entry = zipStream.getNextEntry()) != null) {
        size = Math.toIntExact(entry.getSize());
        byte[] fileData = new byte[size];
        zipStream.read(fileData);
        redis.storeByteFile(entry.getName(), fileData);
      }
      zipStream.close();
    } catch (Exception e) {
      System.err.println("ERROR: Failed to store file in Redis");
    }
  }

}
