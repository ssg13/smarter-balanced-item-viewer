package org.smarterbalanced.itemviewerservice.dal.AmazonApi;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.PropertiesCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClient;
import com.amazonaws.services.sqs.model.CreateQueueRequest;
import com.amazonaws.services.sqs.model.DeleteMessageRequest;
import com.amazonaws.services.sqs.model.DeleteQueueRequest;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import org.smarterbalanced.itemviewerservice.dal.Redis.RedisConnection;
import org.smarterbalanced.itemviewerservice.dal.Zip.StoreZip;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;


public class S3UpdateChecker extends Thread {
  private static final Logger log = Logger.getLogger(S3UpdateChecker.class.getName());
  private String queueUrl;
  private AmazonSQS sqs;
  private RedisConnection redis;
  private AmazonFileApi awsbucket;
  private JedisPool jedisPool;

  /**
   * Creates a new instance of the S3UpdateChecker class.
   * @param queueUrl URL of AWS Message Queue to watch
   */
  public S3UpdateChecker(String queueUrl) {
    AWSCredentials credentials;
    this.queueUrl = queueUrl;
    Region usWest2 = Region.getRegion(Regions.US_WEST_2);
    try {
      credentials = new ProfileCredentialsProvider().getCredentials();
    } catch (Exception e) {
      log.log(Level.SEVERE, e.toString(), e);
      Thread.currentThread().interrupt();
      return;
    }
    this.sqs = new AmazonSQSClient(credentials);
    this.sqs.setRegion(usWest2);
    this.sqs.setEndpoint("sdb.us-west-2.amazonaws.com");
  }

  public S3UpdateChecker(AmazonFileApi bucket) {
    this.awsbucket = bucket;
    this.jedisPool = new JedisPool();
    this.redis = new RedisConnection((this.jedisPool));
  }

  /**
   * Performs a GET request to AWS Message Queue.
   * @return Updates from the Message Queue
   */
  private List<Message> pollForUpdates() {
    List<Message> messages = Collections.emptyList();
    try {
      ReceiveMessageRequest receiveMessageRequest = new ReceiveMessageRequest(this.queueUrl);
      messages = sqs.receiveMessage(receiveMessageRequest).getMessages();
    } catch (AmazonServiceException aex) {
      log.log(Level.SEVERE, "Amazon message queue denied message get request.", aex);
    } catch (AmazonClientException aex) {
      log.log(Level.SEVERE, "Failed to connect to Amazon message queue.", aex);
    }
    return messages;
  }

  /**
   * Performs a DELETE request to AWS Message Queue.
   * @param messages A list of messages to delete from the queue
   */
  private void removeFromQueue(List<Message> messages) {
    for (Message message : messages) {
      String receipt = message.getReceiptHandle();
      try {
        sqs.deleteMessage(new DeleteMessageRequest(this.queueUrl, receipt));
      } catch (AmazonServiceException aex) {
        log.log(Level.WARNING, "Failed to delete message from Amazon message queue.", aex);
      } catch (AmazonClientException aex) {
        log.log(Level.SEVERE, "Failed to connect to Amazon message queue.", aex);
      }
    }
  }

  private void update(String key) {
    ObjectMetadata metadata = awsbucket.getObject(key).getObjectMetadata();
    Jedis jedis = this.jedisPool.getResource();
    String path = System.getProperty("user.home");
    String fileLocation = path + "/" + key;
    byte[] zip;
    System.out.println("Starting to process " + key);
    try {
      if (jedis.exists(key)) {
        if ( !(jedis.get(key).equals(metadata.getLastModified().toString()) )) {
          System.out.println(key + " has been updated.");
          zip = this.awsbucket.getS3File(key);
          FileOutputStream fos = new FileOutputStream(fileLocation);
          fos.write(zip);
          fos.close();
          System.out.println("Wrote file to disk.");
          StoreZip.unpackToRedis(fileLocation, this.redis);
          System.out.println("Stored Zip in Redis.");
          Files.delete(Paths.get(fileLocation));
        }
      } else {
        System.out.println(key + " is not stored in Redis.");
        jedis.set(key, metadata.getLastModified().toString());
        zip = this.awsbucket.getS3File(key);
        FileOutputStream fos = new FileOutputStream(fileLocation);
        fos.write(zip);
        fos.close();
        StoreZip.unpackToRedis(fileLocation, this.redis);
        Files.delete(Paths.get(fileLocation));
      }
    } catch (Exception e) {
      //do something
      System.err.println("An error occured.");
      System.err.println(e.getMessage());
      log.log(Level.SEVERE, "Poll failure", e);
    } finally {
      jedis.set(key, metadata.getLastModified().toString());
    }
    System.out.println("Finished processing " + key);
  }

  /**
   * Periodically polls S3 for changes and triggers Redis updates when necessary.
   */
  public void run() {
    int sleepTime = 7000; //in milliseconds
    List<String> allKeys;

    //First time the thread is started, fetch everything from the S3 bucket it is monitoring.
    for(;;) {
      System.out.println("Checking for updates to packages...");
      allKeys = this.awsbucket.getAllKeys();
      for(String key : allKeys) {
        update(key);
      }
      try {
        Thread.sleep(sleepTime);
      } catch (InterruptedException e) {
        //The thread was interrupted and should exit
        System.err.println("Update checker is exiting.");
        Thread.currentThread().interrupt();
        return;
      }
    }
  }

}
