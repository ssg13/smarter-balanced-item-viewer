package org.smarterbalanced.itemviewerservice.dal.AmazonApi;

import com.amazonaws.services.s3.model.ObjectMetadata;
import org.smarterbalanced.itemviewerservice.dal.Redis.RedisConnection;
import org.smarterbalanced.itemviewerservice.dal.Zip.StoreZip;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * The type S 3 update checker.
 */
public class S3UpdateChecker implements Runnable {
  private static final Logger log = Logger.getLogger("org.smarterbalanced.dal");
  private RedisConnection redis;
  private AmazonFileApi awsbucket;
  private JedisPool jedisPool;


  /**
   * Instantiates a new S3 update checker.
   * This is a runnable object that polls the given S3 bucket for new or updated items.
   * It only runs once, so it should be used with a ScheduledExecutorService.
   * @param bucketName the Amazon S3 bucket to check for updates.
   */
  public S3UpdateChecker(String bucketName) {
    this.awsbucket = new AmazonFileApi(bucketName);
    this.jedisPool = new JedisPool();
    this.redis = new RedisConnection((this.jedisPool));
  }


  private void updateContentPack(String key) throws Exception {
    String path = System.getProperty("user.home");
    String tempFile = path + "/" + key;
    byte[] zip = this.awsbucket.getS3File(key);
    FileOutputStream fos = new FileOutputStream(tempFile);
    fos.write(zip);
    fos.close();
    StoreZip.unpackToRedisHash(key, path);
    Files.delete(Paths.get(tempFile));
  }

  private void checkForUpdate(String key) {
    ObjectMetadata metadata = awsbucket.getObject(key).getObjectMetadata();
    Jedis jedis = this.jedisPool.getResource();
    try {
      if (jedis.exists(key)) {
        if (!(jedis.hget(key, "lastUpdate").equals(metadata.getLastModified().toString()))) {
          log.log(Level.INFO, "Package " + key + " was updated. Fetching latest.");
          updateContentPack(key);
          jedis.hset(key, "lastUpdate", metadata.getLastModified().toString());
        }
      } else {
        log.log(Level.INFO, "New package " + key + "found. Fetching.");
        updateContentPack(key);
        jedis.hset(key, "lastUpdate", metadata.getLastModified().toString());
      }
    } catch (Exception e) {
      log.log(Level.SEVERE, "Failed to update content package " + key, e);
    } finally {
      jedis.close();
    }
  }

  /**
   * Fetches the metadata of all objects in a S3 bucket. Adds any new packages to Redis.
   * Updates and packages already stored in Redis.
   */
  public void run() {
    log.log(Level.INFO, "Running scheduled check for new or updated S3 items.");
    List<String> allKeys = this.awsbucket.getAllKeys();
    for (String key : allKeys) {
      checkForUpdate(key);
    }
    log.log(Level.INFO, "Finished running scheduled check for new or updated S3 items.");
  }
}
