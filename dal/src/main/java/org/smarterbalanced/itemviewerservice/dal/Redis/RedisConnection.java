package org.smarterbalanced.itemviewerservice.dal.Redis;

import org.smarterbalanced.itemviewerservice.dal.Exceptions.FileTooLargeException;
import org.smarterbalanced.itemviewerservice.dal.Exceptions.RedisFileException;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.exceptions.JedisConnectionException;

import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * Store, fetch and delete objects from a Redis cache.
 */
public class RedisConnection {
  private static final Logger log = Logger.getLogger("org.smarterbalanced.dal");
  private JedisPool pool;

  /**
   * Instantiates a new Redis connection object that will use the given connection pool
   * to connect to Redis. Connections are checked out of the pool when a method is called
   * and returned on error or completion.
   *
   * @param pool a pool of Jedis connections the object will use to connect to Redis.
   */
  public RedisConnection(JedisPool pool) {
    this.pool = pool;
  }

  /**
   * Store a byte array holding file data in Redis. The maximum byte array size is 512 MB.
   *
   * @param key      The key used to store the data in Redis.
   *                 If the key is already in Redis the data associated with it will be overwritten.
   * @param fileData The data to store in Redis. Maximum size of 512 MB.
   * @throws RedisFileException if unable to store the file.
   * @throws JedisConnectionException if the Redis connection attempt fails.
   * @throws FileTooLargeException if the file is larger than 512 MB.
   */
  public void storeByteFile(String key, byte[] fileData) throws RedisFileException,
          JedisConnectionException, FileTooLargeException {
    final int maxFileSize = 512000000; //512 MB
    Jedis jedis;
    String result;
    byte[] byteKey = key.getBytes();

    if (fileData.length > maxFileSize) {
      log.log(Level.WARNING,
              "File " + key + " is larger than 512 MB. Too large to store in Redis."
      );
      throw new FileTooLargeException(String.format("File with key %s is larger than 512 MB", key));
    }

    try {
      jedis = this.pool.getResource();
      result = jedis.set(byteKey, fileData);
    } catch (JedisConnectionException e) {
      log.log(Level.SEVERE, "Failed to open connection to Redis.", e);
      throw e;
    }

    jedis.close();

    if (!result.equals("OK")) {
      log.log(Level.WARNING, "Failed to store file in Redis. Reason: " + result);
      throw new RedisFileException(String.format("Failed to store file with key: %s. "
              + "Reason: %s", key, result));
    }
  }

  /**
   * List all keys in Redis.
   *
   * @return a String set containing all of the keys in Redis.
   */
  public Set<String> listKeys() {
    Jedis jedis = this.pool.getResource();
    Set<String> keys = jedis.keys("*");
    jedis.close();
    return keys;
  }

  /**
   * Store text file.
   *
   * @param key      The key used to store the data in Redis.
   *                 If the key is already in Redis the data associated with it will be overwritten.
   * @param fileData The data to store in Redis. Maximum file size of 512 MB.
   * @throws RedisFileException       if unable to store the file.
   * @throws JedisConnectionException if the Redis connection attempt fails.
   * @throws FileTooLargeException    if the file is larger than 512 MB.
   */
  public void storeTextFile(String key, String fileData) throws RedisFileException,
          JedisConnectionException, FileTooLargeException {
    final int maxFileSize = 512000000; //512 MB
    int size = fileData.getBytes().length;
    Jedis jedis;
    String result;

    if (size > maxFileSize) {
      throw new FileTooLargeException(String.format("File with key %s is larger than 512 MB", key));
    }

    try {
      jedis = this.pool.getResource();
      result = jedis.set(key, fileData);
    } catch (JedisConnectionException e) {
      log.log(Level.SEVERE, "Failed to open connection to Redis.", e);
      throw e;
    }

    if (!result.equals("OK")) {
      log.log(Level.WARNING, "Failed to store file:" + key + "Reason" + result);
      throw new RedisFileException(String.format("Failed to store file with key: %s. "
              + "Reason: %s", key, result));
    }
  }

  /**
   * Attempts to fetch the byte data of a file from Redis using the given key.
   * Throws a RedisFileException if there is no file matching the given key.
   *
   * @param key the key for the file to fetch from Redis.
   *            If the key does not exist in Redis an exception will be thrown.
   * @return The contents of the file stored in Redis as a byte array.
   * @throws RedisFileException if there is no file matching that key in Redis.
   * @throws JedisConnectionException if the Redis connection attempt fails.
   */
  public byte[] getByteFile(String key) throws RedisFileException, JedisConnectionException {
    byte[] fileContents;
    Jedis jedis;
    boolean exists;
    byte[] byteKey = key.getBytes();

    try {
      jedis = this.pool.getResource();

      exists = jedis.exists(key);
      if (exists) {
        fileContents = jedis.get(byteKey);
      } else {
        log.log(Level.WARNING, "File with key " + key + " is not in Redis.");
        throw new RedisFileException("File with key " + key + " is not in Redis.");
      }

    } catch (JedisConnectionException e) {
      log.log(Level.SEVERE, "Failed to open connection to Redis.", e);
      throw e;
    }

    jedis.close();

    return fileContents;
  }

  /**
   * Remove the file matching the given key from Redis. If the file does not exist an error is
   * logged and a RedisFileException is thrown.
   * If the file exists and is not deleted an error is logged and a RedisFileException is thrown.
   *
   * @param key the key for the object to delete from Redis.
   *            If the key is invalid nothing will be deleted.
   * @throws RedisFileException if the object is not deleted.
   * @throws JedisConnectionException if the Redis connection attempt fails.
   */
  public void removeFile(String key) throws RedisFileException, JedisConnectionException {
    Jedis jedis;
    Long result = 0L;
    boolean exists;

    try {
      jedis = this.pool.getResource();
      exists = jedis.exists(key);
      if (exists) {
        result = jedis.del(key);
      } else {
        log.log(Level.INFO, "Attempted to delete file not in Redis. Key: " + key);
      }

    } catch (JedisConnectionException e) {
      log.log(Level.SEVERE, "Failed to open connection to Redis.", e);
      throw e;
    }

    jedis.close();

    if (result != 1L) {
      log.log(Level.WARNING, "Failed to delete file with key: " + key + " from Redis.");
      throw new RedisFileException(String.format(
              "Failed to delete file with key: %s from Redis.", key));
    }
  }
}
