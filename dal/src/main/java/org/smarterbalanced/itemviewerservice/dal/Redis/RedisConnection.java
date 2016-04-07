package org.smarterbalanced.itemviewerservice.dal.Redis;

import org.smarterbalanced.itemviewerservice.dal.Exceptions.RedisFileException;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.exceptions.JedisConnectionException;

public class RedisConnection {
  private JedisPool pool;

  public RedisConnection(JedisPool pool) {
    this.pool = pool;
  }

  public void storeFile(String key, byte[] fileData) throws RedisFileException {
    Jedis jedis;
    String result;
    byte[] byteKey = key.getBytes();

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

  public byte[] getByteFile(String key) throws RedisFileException {
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

  public void removeFile(String key) throws RedisFileException {
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
