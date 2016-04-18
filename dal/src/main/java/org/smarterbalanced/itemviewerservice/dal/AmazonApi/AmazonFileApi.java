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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class AmazonFileApi {
  private String bucketName;
  private AmazonS3 s3connection;

  /**
   * Creates a new instance of AmazonFileApi class.
   * @param bucketName Name of the S3 bucket to connect to
   */
  public AmazonFileApi(String bucketName) {
    this.bucketName = bucketName;
    this.s3connection = new AmazonS3Client();
    Region usWest2 = Region.getRegion(Regions.US_WEST_2);
    this.s3connection.setRegion(usWest2);
    //Create the bucket if it does not exist.
    createBucket(bucketName);
  }

  public String getBucketName() {
    return this.bucketName;
  }

  private S3Object getObject(String key) {
    S3Object object = s3connection.getObject(new GetObjectRequest(this.bucketName, key));
    return object;
  }

  private void createBucket(String bucketName) {
    try {
      if(!(this.s3connection.doesBucketExist(bucketName))) {
        this.s3connection.createBucket(new CreateBucketRequest(bucketName));
      }
    } catch (Exception e) {

    }
  }

  /**
   * Lists all objects in the S3 .
   * @return List of all objects in the S3 bucket
   */
  public List<String> getAllKeys() {
    List<String> keys = new ArrayList<String>();

    try {
      ListObjectsRequest listObjectsRequest = new ListObjectsRequest()
              .withBucketName(this.bucketName);
      ObjectListing objectListing;
      do {
        objectListing = s3connection.listObjects(listObjectsRequest);
        for (S3ObjectSummary objectSummary :
                objectListing.getObjectSummaries()) {
          keys.add(objectSummary.getKey());
        }
        listObjectsRequest.setMarker(objectListing.getNextMarker());
      } while (objectListing.isTruncated());
    } catch (AmazonServiceException ase) {
      System.err.println("ERROR: Amazon rejected the connection to S3.");
      System.err.println("Error Message:    " + ase.getMessage());
      System.err.println("HTTP Status Code: " + ase.getStatusCode());
      System.err.println("AWS Error Code:   " + ase.getErrorCode());
      System.err.println("Error Type:       " + ase.getErrorType());
      System.err.println("Request ID:       " + ase.getRequestId());
    } catch (AmazonClientException ace) {
      System.err.println("ERROR: Network error connecting to Amazon S3.");
      System.err.println("Error Message: " + ace.getMessage());
    }
    return keys;
  }

  /**
   * Synchronously downloads a file from S3.
   * @param key The key of the S3 object to download
   * @return Byte array of file
   * @throws FileTooLargeException Throws if file is over 2GB
   * @throws IOException Throws for connection errors with S3
   * @throws NullPointerException Throws when file pointer fails to initialize
   */
  public byte[] getS3File(String key)
          throws FileTooLargeException, IOException, NullPointerException {
    byte[] fileData;
    S3Object object = getObject(key);
    ObjectMetadata objectMetadata = object.getObjectMetadata();
    long awsFileSize = objectMetadata.getContentLength();
    if (awsFileSize > Integer.MAX_VALUE) {
      throw new FileTooLargeException("File is over 2 GB. "
              + "This is too large to hold in memory as a byte array.");
    }

    try {
      InputStream objectFileStream = object.getObjectContent();
      fileData = IOUtils.toByteArray(objectFileStream);
      objectFileStream.close();
    } catch (IOException ex) {
      System.err.println("Failed to open file stream with Amazon S3.");
      throw ex;
    } catch (NullPointerException ex) {
      System.err.println("Amazon S3 file stream is null");
      throw ex;
    }
    return fileData;
  }

  /**
   * Gets a file stream from an object in a S3 bucket.
   * @param key The name of the object to download
   * @return InputStream object representing a file stream of an S3 object
   * @throws NullPointerException Throws if fileStream cannot be initialized
     */
  public InputStream getS3FileStream(String key) throws NullPointerException {
    InputStream objectFileStream;
    S3Object object = getObject(key);
    try {
      objectFileStream = object.getObjectContent();
    } catch (NullPointerException ex) {
      System.err.println("Amazon S3 file stream is null");
      throw ex;
    }
    if (objectFileStream == null) {
      throw new NullPointerException("");
    }
    return objectFileStream;
  }

  public void storeFile(InputStream fileStream, String key, int size) {
    ObjectMetadata objectData = new ObjectMetadata();
    objectData.setContentLength(size);
    try{
      this.s3connection.putObject(new PutObjectRequest(this.bucketName, key, fileStream, objectData));
    } catch (Exception e) {

    }
  }

}
