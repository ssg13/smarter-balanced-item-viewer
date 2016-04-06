package org.smarterbalanced.itemviewerservice.dal.Redis;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.nio.charset.Charset;

public class RedisConnection {
  private JedisPool pool;

  public RedisConnection(JedisPool pool) {
    this.pool = pool;
  }

  public void storeFile(byte[] key, byte[] fileData) {
    Jedis jedis = null;
    String result = null;
    try {
      jedis = this.pool.getResource();
      result = jedis.set(key, fileData);
    } catch (Exception e) {
      System.err.println("ERROR: Failed to get a Redis connection while setting.");
    } finally {
      if (jedis != null) {
        jedis.close();
      }
    }
    if (!result.equals("OK")) {
      System.err.println("ERROR: Failed to store file. Reason " + result);
    }
  }

  public byte[] getFile(byte[] key) {
    byte[] fileContents = null;
    Jedis jedis = null;
    String result = null;
    try {
      jedis = this.pool.getResource();
      fileContents = jedis.get(key);
    } catch (Exception e) {
      System.err.println("ERROR: Failed to get a Redis connection while getting.");
    } finally {
      if (jedis != null) {
        jedis.close();
      }
    }
    return fileContents;
  }

  public void removeFile(String key) {
    Jedis jedis = null;
    Long result = 0L;
    try {
      jedis = this.pool.getResource();
      result = jedis.del(key);
    } catch (Exception e) {
      System.err.println("ERROR: Failed to get a Redis connection while deleting.");
    } finally {
      if (jedis != null) {
        jedis.close();
      }
    }
    if (result != 1L) {
      System.err.println("ERROR: Failed to get delete object.");
    }
  }
}
