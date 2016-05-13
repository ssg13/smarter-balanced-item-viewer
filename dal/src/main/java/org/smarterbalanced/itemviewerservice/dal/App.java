package org.smarterbalanced.itemviewerservice.dal;

import org.smarterbalanced.itemviewerservice.dal.AmazonApi.S3UpdateChecker;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
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
    String packageBucket;

    try {
      packageBucket = getConfigS3Bucket();
    } catch (IOException e) {
      log.log(Level.SEVERE, "Failed to load configuration file.", e);
      packageBucket = "cass-test";
    }

    S3UpdateChecker checker = new S3UpdateChecker(packageBucket);
    ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);

    executor.scheduleAtFixedRate(checker, 0, 2, TimeUnit.MINUTES);

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
