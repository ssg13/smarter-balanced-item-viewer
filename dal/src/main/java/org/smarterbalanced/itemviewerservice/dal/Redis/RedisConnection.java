package org.smarterbalanced.itemviewerservice.dal.Redis;

import redis.clients.jedis.JedisPool;

public class RedisConnection {
  private JedisPool pool;

  public RedisConnection(JedisPool pool) {
    this.pool = pool;
  }

  public void setFile(String key) {

  }

  public byte[] getFile(String key) {
    byte[] fileContents = null;
    return fileContents;
  }

  public void removeFile(String key) {

  }
}
