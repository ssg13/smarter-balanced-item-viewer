package org.smarterbalanced.itemviewerservice.dal.Redis;

import org.smarterbalanced.itemviewerservice.dal.Exceptions.FileTooLargeException;
import org.smarterbalanced.itemviewerservice.dal.Exceptions.RedisFileException;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.exceptions.JedisConnectionException;


/**
 * Store, fetch and delete objects from a Redis cache.
 */
public class RedisConnection {
  private JedisPool pool;

  /**
   * Instantiates a new Redis connection object that will use the given connection pool to connect to Redis.
   * Connections are checked out of the pool when a method is called and returned on error or completion.
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
  public void storeFile(String key, byte[] fileData) throws RedisFileException,
          JedisConnectionException, FileTooLargeException {
    final int maxFileSize = 512000000; //512 MB
    Jedis jedis;
    String result;
    byte[] byteKey = key.getBytes();

    if (fileData.length > maxFileSize) {
      throw new FileTooLargeException(String.format("File with key %s is larger than 512 MB", key));
    }

    try {
      jedis = this.pool.getResource();
      result = jedis.set(byteKey, fileData);
    } catch (JedisConnectionException e) {
      System.err.println("ERROR: Failed to get a Redis connection from pool.");
      throw e;
    }

    jedis.close();

    if (!result.equals("OK")) {
      System.err.println("ERROR: Failed to store file. Reason " + result);
      throw new RedisFileException(String.format("Failed to store file with key: %s. Reason: %s", key, result));
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
      if(exists) {
        fileContents = jedis.get(byteKey);
      }
      else {
        System.err.println(String.format("File with key %s not in Redis.", key));
        throw new RedisFileException(String.format("File with key: %s is not in Redis.", key));
      }

    } catch (JedisConnectionException e) {
      System.err.println("ERROR: Failed to get a Redis connection from connection pool. Reason: " + e.getMessage());
      throw e;
    }

    jedis.close();

    return fileContents;
  }

  /**
   * Remove the file matching the given key from Redis. If the file does not exist an error is logged.
   * If the file exists and is not deleted an error is logged and a RedisFileException is thrown.
   *
   * @param key the key for the object to delete from Redis.
   *            If the key is invalid nothing will be deleted.
   * @throws RedisFileException if the object exists in Redis but is not deleted.
   * @throws JedisConnectionException if the Redis connection attempt fails.
   */
  public void removeFile(String key) throws RedisFileException, JedisConnectionException {
    Jedis jedis;
    Long result = 0L;
    boolean exists;

    try {
      jedis = this.pool.getResource();
      exists = jedis.exists(key);
      if(exists) {
        result = jedis.del(key);
      }
      else {
        System.err.println(String.format("File with key: %s not in Redis", key));
      }

    } catch (JedisConnectionException e) {
      System.err.println("ERROR: Failed to connect to Redis. Reason " + e.getMessage());
      throw e;
    }

    jedis.close();

    if (result != 1L) {
      System.err.println(String.format("ERROR: Failed to delete object with key: %s", key));
      throw new RedisFileException(String.format("Failed to delete file with key: %s", key));
    }
  }
}
