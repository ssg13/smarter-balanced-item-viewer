package org.smarterbalanced.itemviewerservice.dal.AmazonApi;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
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

  public S3Objects listObjects() {
    S3Objects objectList = S3Objects.withPrefix(this.s3connection, this.bucketName, "/");
    return objectList;
  }

  public byte[] getFile(S3Object object) throws FileTooLargeException, IOException, NullPointerException {
    ObjectMetadata objectMetadata = object.getObjectMetadata();
    long awsFileSize = objectMetadata.getContentLength();
    int fileSize;
    try {
      fileSize = toIntExact(awsFileSize);
    } catch (ArithmeticException e) {
      //in this case the file size is > INT MAX. Or greater than ~2 GB.
      throw new FileTooLargeException("File is over 2 GB", e);
    }

    byte[] fileData = new byte[fileSize];
    try {
      InputStream objectFileStream = object.getObjectContent();
      fileData = IOUtils.toByteArray(objectFileStream);
      objectFileStream.close();
    } catch (IOException ex) {
      //occurs in case of IO errors
      //TODO: handle exception
      throw ex;
    } catch (NullPointerException ex) {
      //occurs the case that the file stream fails to init.
      //TODO: handle exception
      throw ex;
    }
    return fileData;
  }
}
