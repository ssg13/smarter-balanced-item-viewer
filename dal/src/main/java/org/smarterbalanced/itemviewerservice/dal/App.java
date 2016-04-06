package org.smarterbalanced.itemviewerservice.dal;

import org.smarterbalanced.itemviewerservice.dal.AmazonApi.S3UpdateChecker;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;

public class App {
  public static void main(String[] args) {
    System.out.println("Hello World!");
    (new S3UpdateChecker("url")).start();

    //Code to get Redis working. Will be replaced.
    byte[] fileArray = null;
    byte[] foo = "foo".getBytes(Charset.forName("UTF-8"));
    try {
      fileArray = Files.readAllBytes(new File("path").toPath());
    } catch (IOException e) {
      System.err.println("Failed to open file");
    }

    JedisPool pool = new JedisPool(new JedisPoolConfig(), "localhost");
    Jedis jedis = null;
    try {
      jedis = pool.getResource();
      jedis.set(foo, fileArray);
      String foobar = jedis.get("foo");
      System.out.println(foobar);
    } finally {
      if (jedis != null) {
        jedis.close();
      }
    }

    pool.destroy();

  }
}
