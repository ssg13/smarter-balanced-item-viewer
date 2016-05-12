package org.smarterbalanced.itemviewerservice.dal;

import org.smarterbalanced.itemviewerservice.dal.AmazonApi.AmazonFileApi;
import org.smarterbalanced.itemviewerservice.dal.AmazonApi.S3UpdateChecker;
import org.smarterbalanced.itemviewerservice.dal.Redis.RedisConnection;
import org.smarterbalanced.itemviewerservice.dal.Zip.StoreZip;
import redis.clients.jedis.JedisPool;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The Application.
 */
public class App {
  private static final Logger log = Logger.getLogger("org.smarterbalanced.dal");
  /**
   * The entry point of application.
   *
   * @param args There are no input arguments needed.
   */
  public static void main(String[] args) {
    try {
      FileHandler logFile = new FileHandler("./dallog.log");
      log.addHandler(logFile);
    } catch (IOException e) {
      log.log(Level.SEVERE, "Unable to create log file.");
      System.exit(1);
    }
    String packageBucket = "";
    List<String> packageKeys = null;
    AmazonFileApi amazonApi;
    JedisPool pool = new JedisPool();
    RedisConnection redis = new RedisConnection(pool);
    long delta;
    long startTime;
    long endTime;
    String path = System.getProperty("user.home") + "/sb-redis-example/";
    Path workingDirectory = Paths.get(path);
    byte[] zip;
    byte[] fileData;

    try {
      packageBucket = getConfigS3Bucket();
    } catch (IOException e) {
      log.log(Level.SEVERE, "Failed to load configuration file.", e);
      System.exit(1);
    }
    amazonApi = new AmazonFileApi(packageBucket);
    S3UpdateChecker checker = new S3UpdateChecker(amazonApi);
    checker.start();

    Thread.State state;
    for(;;) {
      state = checker.getState();
      System.out.println("State is " + state.toString());
      try {
        Thread.sleep(100000);
      } catch (Exception e) {
        //main thread crashed.
      }
    }

    /*



    try {
      packageKeys = amazonApi.getAllKeys();
    } catch (Exception e) {
      System.exit(1);
    }

    try {
      Files.createDirectories(workingDirectory);
    } catch (IOException e) {
      log.log(Level.SEVERE, "Failed to create destination directory for zip file download.", e);
      System.exit(1);
    }

    for (String key : packageKeys) {
      try {
        System.out.println("Starting zip file download from Amazon S3 bucket.");
        startTime = new Date().getTime();
        zip = amazonApi.getS3File(key);
        endTime = new Date().getTime();
        delta = endTime - startTime;
        System.out.println("Finished downloading zip file from Amazon S3.");
        System.out.println("Downloading the zip from Amazon took " + delta + " milliseconds.");
        System.out.println("Starting to write zip file to disk.");
        startTime = new Date().getTime();
        FileOutputStream fos = new FileOutputStream(path + key);
        fos.write(zip);
        fos.close();
        endTime = new Date().getTime();
        delta = endTime - startTime;
        System.out.println("Finished writing zip file to disk.");
        System.out.println("Writing zip file to disk took " + delta + " milliseconds.");
      } catch (Exception e) {
        log.log(Level.SEVERE, "Error fetching files from S3.", e);
        System.exit(1);
      }
    }


    for (String key : packageKeys) {
      try {
        System.out.println("Started unzipping package contents to Redis.");
        startTime = new Date().getTime();
        StoreZip.unpackToRedis(path + key, redis);
        endTime = new Date().getTime();
        delta = endTime - startTime;
        System.out.println("Finished unzipping package contents to Redis.");
        System.out.println("Unpacking the zip file to Redis took " + delta + " milliseconds.");
      } catch (Exception e) {
        log.log(Level.SEVERE, "Failed to store package contents in Redis", e);
        System.exit(1);
      }
    }


    Set<String> keys = redis.listKeys();
    System.out.println("Starting to write files from Redis to disk.");
    startTime = new Date().getTime();
    try {
      for (String key : keys) {
        fileData = redis.getByteFile(key);
        workingDirectory = Paths.get(path + key);
        Files.createDirectories(workingDirectory.getParent());
        Files.write(workingDirectory, fileData);
      }
    } catch (Exception e) {
      log.log(Level.SEVERE, "Failed to extract file from Redis to disk.", e);
      System.exit(1);
    }
    endTime = new Date().getTime();
    delta = endTime - startTime;
    System.out.println("Finished writing files from Redis to disk.");
    System.out.println("Writing files to disk took " + delta + " milliseconds.");
    */

  }

  private static String getConfigS3Bucket() throws IOException {
    String configLocation = "dal-config.properties";
    String bucket;
    Properties dalProperties = new Properties();
    FileInputStream configInput;
    configInput = new FileInputStream(configLocation);
    dalProperties.load(configInput);
    bucket = dalProperties.getProperty("S3bucket");
    configInput.close();
    return bucket;
  }

}
