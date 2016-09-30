package org.smarterbalanced.itemviewerservice.core;

import static org.apache.http.HttpHeaders.USER_AGENT;

import org.apache.commons.io.FileUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smarterbalanced.itemviewerservice.dal.AmazonApi.AmazonFileApi;
import org.smarterbalanced.itemviewerservice.dal.Config.SettingsReader;
import org.smarterbalanced.itemviewerservice.dal.Zip.StoreZip;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;


/**
 * The type S3 update checker.
 */
public class S3UpdateChecker {
  private static final Logger logger = LoggerFactory.getLogger(S3UpdateChecker.class);
  private AmazonFileApi awsbucket;


  /**
   * Instantiates a new S3 update checker.
   * This is a runnable object that polls the given S3 bucket for new or updated items.
   * It only runs once, so it should be used with a ScheduledExecutorService.
   */
  public S3UpdateChecker() {
    this.awsbucket = new AmazonFileApi();
  }

  /*
  Deletes the contents of the destination directory, then moves the content of the source
   directory into the destination directory
  */
  private void replaceContent(File sourceDir, File destinationDir) throws IOException {
    if (!destinationDir.isDirectory()) {
      throw new IOException("Content directory is not a directory.");
    }
    if (!destinationDir.canWrite()) {
      throw new IOException("Content directory is not writable.");
    }
    if (!sourceDir.isDirectory()) {
      throw new IOException("Source directory is not a directory");
    }
    if (!sourceDir.canRead()) {
      throw new IOException("Source directory is not readable.");
    }

    FileUtils.cleanDirectory(destinationDir);
    FileUtils.copyDirectory(sourceDir, destinationDir);
  }


  /*
  Generates a hashmap of filename/last updated
   */
  private HashMap<String, String> readContentPackageList() throws Exception {
    HashMap<String, String> contentInfo = new HashMap<>();
    InputStream inputStream = null;
    Properties properties = new Properties();
    try {
      String irisContentPath = SettingsReader.readIrisContentPath();
      File contentList = new File(irisContentPath + "/packageList");
      if (!contentList.exists() && !contentList.isDirectory()) {
        //if there is no list of content packages return an empty list
        return contentInfo;
      }
      inputStream = new FileInputStream(contentList);
      properties.load(inputStream);
      inputStream.close();
      Set<Object> keys = properties.keySet();
      for (Object key: keys) {
        contentInfo.put(key.toString(), properties.getProperty(key.toString()));
      }
    } catch (Exception e) {
      logger.error("Unable to load details of current content.", e);
      throw e;
    }
    return contentInfo;
  }

  private void generateContentPackageList(HashMap<String,String> contentInfo,
                                          File contentDirectory) {
    Properties properties = new Properties();
    OutputStream outputStream = null;
    for (Map.Entry<String, String> entry : contentInfo.entrySet()) {
      properties.setProperty(entry.getKey(), entry.getValue());
    }
    try {
      outputStream = new FileOutputStream(contentDirectory + "/packageList");
      properties.store(outputStream, null);
      outputStream.close();
    } catch (IOException e) {
      logger.error("Unable to write list of package contents.");
    }
  }


  private boolean updateContentPackages(HashMap<String, String> newPackageMap) {
    String path = System.getProperty("user.home") + "/tempContentLocation";
    File tempContentDir = new File(path);
    File contentDir;
    HashMap<String,String> oldPackageMap;
    try {
      contentDir = new File(SettingsReader.readIrisContentPath());
    } catch (Exception e) {
      logger.error("Unable to read iris content location from config file. "
              + "Terminating content package update.", e);
      return false;
    }

    try {
      oldPackageMap = readContentPackageList();
      if (!newPackageMap.equals(oldPackageMap)) {
        if (!tempContentDir.exists()) {
          if (!tempContentDir.mkdir()) {
            SecurityException ex = new SecurityException(
                    "Permissions disallow creating temporary content directory"
                    + path);
            logger.error("Unable to create temporary content directory.", ex);

            return false;
          }
        }
        List<String> allKeys = this.awsbucket.getAllKeys();
        for (String key : allKeys) {
          awsbucket.writeS3FileToDisk(key, path);
        }
        for (String key : allKeys) {
          StoreZip.extractZip(tempContentDir.getPath() + "/" + key, tempContentDir.getPath());
        }
        generateContentPackageList(newPackageMap, tempContentDir);
        replaceContent(tempContentDir, contentDir);
        FileUtils.cleanDirectory(tempContentDir);
        logger.info("New content packages were downloaded from Amazon S3.");
        return true;
      }
    } catch (Exception e) {
      logger.error("Unable to fetch updated content from Amazon S3.", e);
    }
    logger.info("No new content packages were found.");
    return false;
  }

  /**
   * Fetches the metadata of all objects in a S3 bucket.
   * If any of the packages in S3 have changed, or new ones have been added
   * then the contents of the S3 bucket are downloaded, the old content directory is purged,
   * and the new content is copied over.
   */
  public void checkForUpdates() {
    logger.info("Running scheduled check for new or updated S3 items.");
    HashMap<String, String> timestamps = awsbucket.getContentLastUpdated();
    boolean newContent = updateContentPackages(timestamps);
    if (newContent) {
      String url = "http://localhost/reload";
      HttpClient client = HttpClientBuilder.create().build();
      HttpGet request = new HttpGet(url);
      request.addHeader("User-Agent", USER_AGENT);
    }

    logger.info("Finished running scheduled check for new or updated S3 items.");
  }
}
