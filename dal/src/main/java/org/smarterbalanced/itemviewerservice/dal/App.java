package org.smarterbalanced.itemviewerservice.dal;

import org.smarterbalanced.itemviewerservice.dal.AmazonApi.S3UpdateChecker;
import org.smarterbalanced.itemviewerservice.dal.Redis.RedisConnection;

import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class App {
  public static void main(String[] args) {
    System.out.println("Hello World!");

    //TODO: split this out and hook up with Amazon API instead of local filesystem.
    byte[] fileArray = null;
    String fileString;
    byte[] redisResult;
    String key = "bar";
    byte[] byteKey = key.getBytes();
    String redisStringResult;

    try {
      fileArray = Files.readAllBytes(new File("/path/file").toPath());
    } catch (IOException e) {
      System.err.println("Failed to open file: " + e.getMessage());
      System.exit(1);
    }

    fileString = new String(fileArray);

    System.out.println("Contents before storing: " + fileString);

    JedisPool pool = new JedisPool(new JedisPoolConfig(), "localhost");
    RedisConnection redis = new RedisConnection(pool);

    redis.storeFile(byteKey, fileArray);

    redisResult = redis.getFile(byteKey);
    redisStringResult = new String(redisResult);

    System.out.println("After storing: " + redisStringResult);

    pool.destroy();
  }
}
