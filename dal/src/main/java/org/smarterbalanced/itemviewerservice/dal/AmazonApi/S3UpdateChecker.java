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


public class S3UpdateChecker extends Thread {
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
      System.err.println("ERROR: Unable to load Amazon credentials. "
          + "This will prevent connection to the Amazon API.");
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
      System.err.format("ERROR: Amazon rejected message get request. "
          + "Reason: %s%n", aex.getMessage());
    } catch (AmazonClientException aex) {
      System.err.format("ERROR: Unable to communicate with the Amazon API. "
          + "Reason: %s%n", aex.getMessage());
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
        System.err.format("ERROR: Amazon denied the request to delete the message. "
            + "Reason: %s%n", aex.getErrorMessage());
      } catch (AmazonClientException aex) {
        System.err.format("ERROR: Unable to communicate with the Amazon API. "
            + "Reason: %s%n", aex.getMessage());
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
