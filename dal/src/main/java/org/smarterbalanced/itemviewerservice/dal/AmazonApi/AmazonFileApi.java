package org.smarterbalanced.itemviewerservice.dal.AmazonApi;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.iterable.S3Objects;
import com.amazonaws.services.s3.model.*;
import com.amazonaws.util.IOUtils;

import org.smarterbalanced.itemviewerservice.dal.Exceptions.FileTooLargeException;

import java.io.IOException;
import java.io.InputStream;
import static java.lang.Math.toIntExact;

public class AmazonFileApi {
  private String bucketName;
  private AmazonS3 s3connection;

  public AmazonFileApi(String bucketName) {
    this.bucketName = bucketName;
    this.s3connection = new AmazonS3Client();
    Region usWest2 = Region.getRegion(Regions.US_WEST_2);
    this.s3connection.setRegion(usWest2);
  }

  public String getBucketName() {
    return this.bucketName;
  }

  public S3Object getObject(String key) {
    S3Object object = s3connection.getObject(new GetObjectRequest(this.bucketName, key));
    return object;
  }

  public void listObjectKeys() {

    try {
      System.out.println("Listing objects");

      ListObjectsRequest listObjectsRequest = new ListObjectsRequest()
              .withBucketName(this.bucketName);
      ObjectListing objectListing;
      do {
        objectListing = s3connection.listObjects(listObjectsRequest);
        for (S3ObjectSummary objectSummary :
                objectListing.getObjectSummaries()) {
          System.out.println(" - " + objectSummary.getKey() + "  " +
                  "(size = " + objectSummary.getSize() +
                  ")");
        }
        listObjectsRequest.setMarker(objectListing.getNextMarker());
      } while (objectListing.isTruncated());
    } catch (AmazonServiceException ase) {
      System.out.println("Caught an AmazonServiceException, " +
              "which means your request made it " +
              "to Amazon S3, but was rejected with an error response " +
              "for some reason.");
      System.out.println("Error Message:    " + ase.getMessage());
      System.out.println("HTTP Status Code: " + ase.getStatusCode());
      System.out.println("AWS Error Code:   " + ase.getErrorCode());
      System.out.println("Error Type:       " + ase.getErrorType());
      System.out.println("Request ID:       " + ase.getRequestId());
    } catch (AmazonClientException ace) {
      System.out.println("Caught an AmazonClientException, " +
              "which means the client encountered " +
              "an internal error while trying to communicate" +
              " with S3, " +
              "such as not being able to access the network.");
      System.out.println("Error Message: " + ace.getMessage());
    }
  }

  public byte[] getFile(String key) throws FileTooLargeException, IOException, NullPointerException {
    S3Object object = getObject(key);
    ObjectMetadata objectMetadata = object.getObjectMetadata();
    long awsFileSize = objectMetadata.getContentLength();
    if(awsFileSize > Integer.MAX_VALUE){
      throw new FileTooLargeException("File is over 2 GB");
    }

    byte[] fileData;
    try {
      InputStream objectFileStream = object.getObjectContent();
      fileData = IOUtils.toByteArray(objectFileStream);
      objectFileStream.close();
    } catch (IOException ex) {
      //occurs in case of IO errors
      //TODO: handle exception
      System.err.println("Failed to open file stream.");
      throw ex;
    } catch (NullPointerException ex) {
      //occurs the case that the file stream fails to init.
      System.err.println("File stream is null");
      //TODO: handle exception
      throw ex;
    }
    return fileData;
  }
}
