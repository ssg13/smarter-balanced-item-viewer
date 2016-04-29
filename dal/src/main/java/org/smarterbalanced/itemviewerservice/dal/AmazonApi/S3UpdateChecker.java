package org.smarterbalanced.itemviewerservice.dal.AmazonApi;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.PropertiesCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClient;
import com.amazonaws.services.sqs.model.CreateQueueRequest;
import com.amazonaws.services.sqs.model.DeleteMessageRequest;
import com.amazonaws.services.sqs.model.DeleteQueueRequest;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.SendMessageRequest;

import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;


public class S3UpdateChecker extends Thread {
  private static final Logger log = Logger.getLogger(S3UpdateChecker.class.getName());
  private String queueUrl;
  private AmazonSQS sqs;

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

  /**
   * Periodically polls S3 for changes and triggers Redis updates when necessary.
   */
  public void run() {
    int sleepTime = 7000; //4 seconds
    while (true) {
      List<Message> messages = pollForUpdates();
      /*
      TODO: Act on new messages in the queue.
      Once we have determined the best way to unpack and store packages
      we need to use this to process new packages that are added to the S3 bucket
      while the application is running.
       */
      try {
        Thread.sleep(sleepTime);
      } catch (InterruptedException e) {
        //The thread was interrupted and should exit
        Thread.currentThread().interrupt();
        return;
      }
    }
  }

}
