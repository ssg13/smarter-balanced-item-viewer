package org.smarterbalanced.itemviewerservice.dal;

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
    String redisStringResult = "";

    try {
      fileArray = Files.readAllBytes(new File("path").toPath());
    } catch (IOException e) {
      System.err.println("Failed to open file: " + e.getMessage());
      System.exit(1);
    }

    fileString = new String(fileArray);

    System.out.println("Contents before storing: " + fileString);

    JedisPool pool = new JedisPool(new JedisPoolConfig(), "localhost");
    RedisConnection redis = new RedisConnection(pool);

    try{
      redis.storeByteFile(key, fileArray);
    } catch (Exception e) {
      System.err.println("Failed to store file");
    }

    try {
      redisResult = redis.getByteFile(key);
      redisStringResult = new String(redisResult);
    } catch (Exception e) {
      System.err.println("Failed to get file");
    }

    System.out.println("After storing: " + redisStringResult);

    try {
      redis.removeFile("baz");
    } catch (Exception e) {
      System.err.println("Failed to delete");
    }

    pool.destroy();
  }
}
