package org.smarterbalanced.itemviewerservice.dal;


import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.PropertiesCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;

import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
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

public class S3UpdateChecker extends Thread {
  AWSCredentials credentials;
  String queueUrl;
  AmazonSQS sqs;

  S3UpdateChecker() {
    try {
      this.credentials = new ProfileCredentialsProvider().getCredentials();
    } catch (Exception e) {
      throw new AmazonClientException("Unable to load Amazon credentials");
    }
    this.sqs = new AmazonSQSClient(this.credentials);
  }

  private List<Message> pollForUpdates() {
    List<Message> messages = Collections.emptyList();
    try{
      ReceiveMessageRequest receiveMessageRequest = new ReceiveMessageRequest(this.queueUrl);
      messages = sqs.receiveMessage(receiveMessageRequest).getMessages();
    } catch (Exception e) {
      //handle exception
      System.err.format("ERROR: unable to fetch messages from Amazon. Reason: %s", e.getMessage());
    }
    return messages;
  }

  public void run() {
    List<Message> messages = Collections.emptyList();
    int sleepTime = 4000; //4 seconds
    while(true) {
      messages = pollForUpdates();
      //TODO: replace with using messages to update index
      for(Message message : messages) {
        System.out.println("Message Body: " + message.getBody());
        for (Entry<String, String> entry : message.getAttributes().entrySet()) {
          System.out.println("Message Attribute");
          System.out.println("Name: " + entry.getKey());
          System.out.println("Value: " + entry.getValue());
        }
      }
      try {
        Thread.sleep(sleepTime);
      } catch (Exception e) {

      }
    }
  }

  public static void main(String args[]) {
    (new S3UpdateChecker()).start();
  }

}
