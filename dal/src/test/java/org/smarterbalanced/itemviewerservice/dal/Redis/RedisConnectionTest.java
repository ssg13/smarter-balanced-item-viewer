package org.smarterbalanced.itemviewerservice.dal.Redis;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import org.smarterbalanced.itemviewerservice.dal.Exceptions.FileTooLargeException;
import org.smarterbalanced.itemviewerservice.dal.Exceptions.RedisFileException;

import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.exceptions.JedisConnectionException;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;


public class RedisConnectionTest {

  JedisPool pool;
  RedisConnection redis;

  @Before
  public void setup() {
    this.pool = new JedisPool(new JedisPoolConfig(), "localhost");
    this.redis = new RedisConnection(this.pool);

    final String key = "validTestKey";
    byte[] testData =  "Some Test String".getBytes();

    try{
      this.redis.storeByteFile(key, testData);
    } catch(Exception e) {
      assertTrue("Failed pre test setup. Please make sure Redis is running.", false);
    }
  }

  @Test
  public void storeGoodByteFileTest() throws RedisFileException, FileTooLargeException, JedisConnectionException {
    final String key = "goodByteTestKey";
    final byte[] goodTestData = "Donec quis viverra justo. Vivamus dignissim euismod ornare. Fusce ante elit, euismod eu ligula id, tempus vehicula metus. Integer consectetur ornare mollis. Ut pharetra erat at elit dapibus, vel ultrices sapien maximus. Maecenas nec felis blandit dui volutpat volutpat. Donec massa erat, porttitor et fringilla eu, laoreet vitae justo. Sed quis urna malesuada, pharetra justo nec, posuere lacus. Suspendisse quis aliquam est. Praesent pellentesque, ipsum sed aliquet eleifend, est nulla orci aliquam.".getBytes();
    this.redis.storeByteFile(key, goodTestData);
  }

  @Test(expected = FileTooLargeException.class)
  public void storeBadByteFileTest() throws RedisFileException, JedisConnectionException, FileTooLargeException {
    final String key = "badTestKey";
    byte[] badTestData = new byte[1000000000];
    this.redis.storeByteFile(key, badTestData);
  }

  @Test
  public void storeGoodStringFileTest() throws RedisFileException, FileTooLargeException, JedisConnectionException {
    final String key = "goodStringTestKey";
    final String goodTestString = "This is a valid test string.";
    this.redis.storeTextFile(key, goodTestString);
  }

  @Test(expected = RedisFileException.class)
  public void getInvalidKey() throws RedisFileException, JedisConnectionException {
    final String key = "invalidTestKey";
    byte[] fileContents = this.redis.getByteFile(key);
  }

  @Test
  public void getValidKey() throws RedisFileException, JedisConnectionException {
    final String key = "validTestKey";
    byte[] testData = "Some Test String".getBytes();
    byte[] returnData = this.redis.getByteFile(key);
    assertArrayEquals(testData, returnData);
  }

  @Test(expected = RedisFileException.class)
  public void removeInvalidFile() throws RedisFileException, JedisConnectionException {
    final String key = "invalidTestKey";

    ByteArrayOutputStream systemOutput = new ByteArrayOutputStream();
    System.setErr(new PrintStream(systemOutput));

    this.redis.removeFile(key);

    assertEquals("File with key: invalidTestKey not in Redis", systemOutput.toString());

  }

  @Test
  public void removeValidFile() throws RedisFileException, JedisConnectionException {
    final String key = "validTestKey";
    this.redis.removeFile(key);
  }
  @After
  public void cleanup() {
    this.pool.destroy();
  }

}
