package org.smarterbalanced.itemviewerservice.dal.AmazonApi;



import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.PropertiesCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.AWSCredentials;
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

  public S3UpdateChecker(String queueUrl) {
    AWSCredentials credentials;
    this.queueUrl = queueUrl;
    try {
      credentials = new ProfileCredentialsProvider().getCredentials();
    } catch (Exception e) {
      System.err.println("ERROR: Unable to load Amazon credentials. This will prevent connection to the Amazon API.");
      Thread.currentThread().interrupt();
      return;
    }
    this.sqs = new AmazonSQSClient(credentials);
  }

  private List<Message> pollForUpdates() {
    List<Message> messages = Collections.emptyList();
    try{
      ReceiveMessageRequest receiveMessageRequest = new ReceiveMessageRequest(this.queueUrl);
      messages = sqs.receiveMessage(receiveMessageRequest).getMessages();
    } catch (AmazonServiceException aex) {
      System.err.format("ERROR: Amazon rejected message get request. Reason: %s%n", aex.getErrorMessage());
    } catch (AmazonClientException aex) {
      System.err.format("ERROR: Unable to communicate with the Amazon API. Reason: %s%n", aex.getMessage());
    }
    return messages;
  }

  private void removeFromQueue(List<Message> messages) {
    for (Message message : messages) {
      String receipt = message.getReceiptHandle();
      try {
        sqs.deleteMessage(new DeleteMessageRequest(this.queueUrl, receipt));
      } catch (AmazonServiceException aex) {
        System.err.format("ERROR: Amazon denied the request to delete the message. Reason: %s%n", aex.getErrorMessage());
      } catch (AmazonClientException aex) {
        System.err.format("ERROR: Unable to communicate with the Amazon API. Reason: %s%n", aex.getMessage());
      }
    }
  }

  public void updateRedisIndex() {
    /*TODO: Once Redis is implemented, use this to sync S3 file uploads/deletions with Redis
    If a new file is added to S3 check if it is in the Redis cache. If not add it, else ignore.
    If a file is removed from S3 check if it is in the Redis cache. If  not, ignore, else delete it.
     */
  }

  public void run() {
    int sleepTime = 4000; //4 seconds
    while (true) {
      List<Message> messages = pollForUpdates();
      //TODO: Replace for loop with updateRedisIndex method once implemented
      for (Message message : messages) {
        System.out.println("Message Body: " + message.getBody());
        for (Entry<String, String> entry : message.getAttributes().entrySet()) {
          System.out.println("Message Attribute");
          System.out.println("Name: " + entry.getKey());
          System.out.println("Value: " + entry.getValue());
        }
      }
      removeFromQueue(messages);
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
