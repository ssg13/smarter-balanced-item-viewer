package org.smarterbalanced.itemviewerservice.dal;

import org.smarterbalanced.itemviewerservice.dal.AmazonApi.AmazonFileApi;
import org.smarterbalanced.itemviewerservice.dal.AmazonApi.S3UpdateChecker;
import org.smarterbalanced.itemviewerservice.dal.Redis.RedisConnection;
import org.smarterbalanced.itemviewerservice.dal.Zip.StoreZip;
import redis.clients.jedis.JedisPool;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.Set;

/**
 * The Application.
 */
public class App {

  /**
   * The entry point of application.
   *
   * @param args There are no input arguments needed.
   */
  public static void main( String[] args ) {
    System.out.println( "Hello World!" );
    String fileName = "IrpContentPackage.zip";
    String packageBucket = "cass-test";
    AmazonFileApi amazonApi = new AmazonFileApi(packageBucket);
    JedisPool pool = new JedisPool();
    RedisConnection redis = new RedisConnection(pool);
    long delta;
    long startTime;
    long endTime;
    String path = "C:\\Users\\smithgar\\Downloads\\scratch\\";
    byte[] zip;
    try {
      System.out.println("Starting Download");
      startTime = new Date().getTime();
      zip = amazonApi.getS3File("IrpContentPackage.zip");
      endTime = new Date().getTime();
      delta = endTime - startTime;
      System.out.println("Downloading the zip from Amazon took " + delta + " milliseconds.");
      InputStream zipStream = new ByteArrayInputStream(zip);
      startTime = new Date().getTime();
      StoreZip.unpackToRedis(zipStream, redis);
      endTime = new Date().getTime();
      delta = endTime - startTime;
      System.out.println("Storing the zip to Redis took " + delta + " milliseconds.");
    } catch (Exception e) {
      System.out.println("Error storing files in Redis.");
      System.out.println(e.getMessage());
      System.exit(1);
    }

    Set<String> keys = redis.listKeys();
    try {
      for (String key : keys) {
        byte[] fileData = redis.getByteFile(key);
        Path file = Paths.get(path + key);
        Files.createDirectories(file.getParent());
        Files.write(file, fileData);
      }
    } catch (Exception e) {
      System.err.println("Failed to extract file from Redis to disk.");
      System.err.println(e.getMessage());
      e.printStackTrace();
      System.exit(1);
    }

    /*
    try {
      zip = amazonApi.getS3File("IrpContentPackage.zip");
      InputStream zipStream = new ByteArrayInputStream(zip);
      startTime = new Date().getTime();
      StoreZip.unpackToBucket(zipStream, "test-contentpack");
      endTime = new Date().getTime();
      delta = endTime - startTime;
      System.out.println("Storing the zip contents to an Amazon bucket took "
              + delta + " milliseconds.");
    } catch (Exception e) {
      System.out.println("Error storing files in S3.");
      System.out.println(e.getMessage());
      System.exit(1);
    }
    */
  }

}
