package org.smarterbalanced.itemviewerservice.dal;

import org.smarterbalanced.itemviewerservice.dal.AmazonApi.AmazonFileApi;
import org.smarterbalanced.itemviewerservice.dal.AmazonApi.S3UpdateChecker;
import org.smarterbalanced.itemviewerservice.dal.Redis.RedisConnection;
import org.smarterbalanced.itemviewerservice.dal.Zip.StoreZip;
import redis.clients.jedis.JedisPool;

import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
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
    System.out.println( "Starting redis storage example." );
    String fileName = "IrpContentPackage.zip";
    String packageBucket = "cass-test";
    AmazonFileApi amazonApi = new AmazonFileApi(packageBucket);
    JedisPool pool = new JedisPool();
    RedisConnection redis = new RedisConnection(pool);
    long delta;
    long startTime;
    long endTime;
    System.getProperty("user.home");
    String path = System.getProperty("user.home") + "/sb-redis-example/";
    Path filePath = Paths.get(path);
    byte[] zip;
    byte[] fileData;

    try {
      Files.createDirectories(filePath);
    } catch (IOException e) {
      System.err.println("Failed to create destination directory for zip file download.");
      System.exit(1);
    }
    try {
      System.out.println("Starting zip file download from Amazon S3 bucket.");
      startTime = new Date().getTime();
      zip = amazonApi.getS3File(fileName);
      endTime = new Date().getTime();
      delta = endTime - startTime;
      System.out.println("Finished downloading zip file from Amazon S3.");
      System.out.println("Downloading the zip from Amazon took " + delta + " milliseconds.");
      System.out.println("Starting to write zip file to disk.");
      startTime = new Date().getTime();
      FileOutputStream fos = new FileOutputStream(path + fileName);
      fos.write(zip);
      fos.close();
      endTime = new Date().getTime();
      delta = endTime - startTime;
      System.out.println("Finished writing zip file to disk.");
      System.out.println("Writing zip file to disk took " + delta + " milliseconds.");
    } catch (Exception e) {
      System.out.println("Error fetching files from S3.");
      System.out.println(e.getMessage());
      System.exit(1);
    }

    try {
      System.out.println("Started unzipping package contents to Redis.");
      startTime = new Date().getTime();
      StoreZip.unpackToRedis(path + fileName, redis);
      endTime = new Date().getTime();
      delta = endTime - startTime;
      System.out.println("Finished unzipping package contents to Redis.");
      System.out.println("Unpacking the zip file to Redis took " + delta + " milliseconds.");
    } catch (Exception e) {
      System.err.println("Failed to store package contents in Redis");
      System.err.println(e.getMessage());
      System.exit(1);
    }



    Set<String> keys = redis.listKeys();
    System.out.println("Starting to write files from Redis to disk.");
    startTime = new Date().getTime();
    try {
      for (String key : keys) {
        fileData = redis.getByteFile(key);
        filePath = Paths.get(path + key);
        Files.createDirectories(filePath.getParent());
        Files.write(filePath, fileData);
      }
    } catch (Exception e) {
      System.err.println("Failed to extract file from Redis to disk.");
      System.err.println(e.getMessage());
      e.printStackTrace();
      System.exit(1);
    }
    endTime = new Date().getTime();
    delta = endTime - startTime;
    System.out.println("Finished writing files from Redis to disk.");
    System.out.println("Writing files to disk took " + delta + " milliseconds.");

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
