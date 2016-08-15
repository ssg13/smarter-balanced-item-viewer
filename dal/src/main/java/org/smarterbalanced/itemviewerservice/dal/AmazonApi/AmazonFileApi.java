package org.smarterbalanced.itemviewerservice.dal.AmazonApi;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.regions.RegionUtils;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectSummary;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smarterbalanced.itemviewerservice.dal.Config.SettingsReader;
import org.smarterbalanced.itemviewerservice.dal.Exceptions.FileTooLargeException;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


/**
 * The type Amazon file api.
 */
public class AmazonFileApi {
  private static final Logger logger = LoggerFactory.getLogger(AmazonFileApi.class);
  private String bucketName;
  private AmazonS3 s3connection;

  /**
   * Creates a new instance of AmazonFileApi class.
   */
  public AmazonFileApi() {
    this.bucketName = SettingsReader.get("S3bucket");
    this.s3connection = new AmazonS3Client();
    String region = SettingsReader.get("S3region");
    this.s3connection.setRegion(RegionUtils.getRegion(region));
  }

  /**
   * Gets object.
   *
   * @param key the key for an item in the AWS S3 bucket
   * @return S3 object
   */
  public S3Object getObject(String key) {
    S3Object object = s3connection.getObject(new GetObjectRequest(this.bucketName, key));
    return object;
  }

  /**
   * Lists all objects in the S3 bucket.
   *
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
      logger.error("Amazon rejected the connection to S3.", ase);
      throw ase;
    } catch (AmazonClientException ace) {
      logger.error("Network error connecting to Amazon S3.");
      throw ace;
    }
    return keys;
  }

  /**
   * Gets the the timestamps for items in the AWS S3 bucket.
   *
   * @return a hashmap with the key the package name and the value the last updated time stamp
   */
  public HashMap<String, String> getContentLastUpdated() {
    HashMap<String, String> packageTimestamps = new HashMap<>();

    try {
      ListObjectsRequest listObjectsRequest = new ListObjectsRequest()
              .withBucketName(this.bucketName);
      ObjectListing objectListing;
      do {
        objectListing = s3connection.listObjects(listObjectsRequest);
        for (S3ObjectSummary objectSummary :
                objectListing.getObjectSummaries()) {
          packageTimestamps.put(objectSummary.getKey(), objectSummary.getLastModified().toString());
        }
        listObjectsRequest.setMarker(objectListing.getNextMarker());
      } while (objectListing.isTruncated());
    } catch (AmazonServiceException ase) {
      logger.error("Amazon rejected the connection to S3.", ase);
      throw ase;
    } catch (AmazonClientException ace) {
      logger.error("Network error connecting to Amazon S3.");
      throw ace;
    }
    return packageTimestamps;
  }

  /**
   * Synchronously downloads a file from S3.
   *
   * @param key The key of the S3 object to download
   * @return Byte array of file
   * @throws FileTooLargeException    Throws if file is over 2GB
   * @throws IOException              Throws for connection errors with S3
   * @throws NullPointerException     Throws when file pointer fails to initialize
   * @throws NoSuchAlgorithmException the no such algorithm exception
   */
  public byte[] getS3File(String key)
          throws FileTooLargeException, IOException, NullPointerException,
          NoSuchAlgorithmException {
    byte[] fileData;
    String awsMD5;
    String downloadMD5;
    MessageDigest md5;
    InputStream objectFileStream;
    S3Object object = getObject(key);
    ObjectMetadata objectMetadata = object.getObjectMetadata();
    long awsFileSize = objectMetadata.getContentLength();
    if (awsFileSize > Integer.MAX_VALUE) {
      throw new FileTooLargeException("File is over 2 GB. "
              + "This is too large to hold in memory as a byte array.");
    }

    try {
      awsMD5 = object.getObjectMetadata().getUserMetaDataOf("md5");
      awsMD5 = awsMD5.replaceAll("\\s+", ""); /* Strip any extra whitespace. */
      if (awsMD5.length() != 32) {
        logger.warn("MD5 metadata for package " + key + " is not 32 characters.");
      }
      md5 = MessageDigest.getInstance("MD5");
      objectFileStream = object.getObjectContent();
      DigestInputStream digestStream = new DigestInputStream(objectFileStream, md5);
      fileData = IOUtils.toByteArray(digestStream);
      downloadMD5 = Hex.encodeHexString(md5.digest());
      digestStream.close();
      objectFileStream.close();
      if (!downloadMD5.equals(awsMD5)) { /* Retry the download. */
        logger.warn(
                "First download attempt MD5 checksums for package " + key + " do not match.\n"
                        + "Calculated MD5 checksum for download: " + downloadMD5 + "\n"
                        + "MD5 checksum from Amazon S3 header:   " + awsMD5
        );
        object = getObject(key);
        objectFileStream = object.getObjectContent();
        digestStream = new DigestInputStream(objectFileStream, md5);
        fileData = IOUtils.toByteArray(digestStream);
        downloadMD5 = Hex.encodeHexString(md5.digest());
        digestStream.close();
        objectFileStream.close();
        if (!downloadMD5.equals(awsMD5)) {
          /* MD5 checksums still don't match. Log an error and continue. */
          logger.warn(
                  "Second download attempt MD5 checksums for package " + key + " do not match.\n"
                          + "Calculated MD5 checksum for download: " + downloadMD5 + "\n"
                          + "MD5 checksum from Amazon S3 header:   " + awsMD5
          );
        }
      }
    } catch (IOException ex) {
      logger.error("Failed to open file stream with Amazon S3.", ex);
      throw ex;
    } catch (NullPointerException ex) {
      logger.error("Amazon S3 file stream is null", ex);
      throw ex;
    } catch (NoSuchAlgorithmException ex) {
      logger.warn("MD5 checksum generation is not supported.", ex);
      throw ex;
    }
    return fileData;
  }

  /**
   * Write s 3 file to disk.
   *
   * @param key      the key for the file in the S3 bucket
   * @param location the location on the local file system to write the file too
   * @throws FileTooLargeException    the file is too large to download
   * @throws IOException              Unable to download the file due to IO errors
   * @throws NullPointerException     One of the file streams is null
   * @throws NoSuchAlgorithmException Unable to generate the MD5 sum for the file
   */
  public void writeS3FileToDisk(String key, String location) throws FileTooLargeException,
          IOException, NullPointerException, NoSuchAlgorithmException {
    String awsMD5;
    String downloadMD5;
    MessageDigest md5;
    InputStream objectFileStream;
    OutputStream outputStream;
    String outputPath = location + "/" + key;
    S3Object object = getObject(key);
    try {
      outputStream = new FileOutputStream(outputPath);
      awsMD5 = object.getObjectMetadata().getUserMetaDataOf("md5");
      awsMD5 = awsMD5.replaceAll("\\s+", ""); /* Strip any extra whitespace. */
      if (awsMD5.length() != 32) {
        logger.warn("MD5 metadata for package " + key + " is not 32 characters.");
      }
      md5 = MessageDigest.getInstance("MD5");
      objectFileStream = object.getObjectContent();
      DigestInputStream digestStream = new DigestInputStream(objectFileStream, md5);
      IOUtils.copy(digestStream, outputStream);
      downloadMD5 = Hex.encodeHexString(md5.digest());
      digestStream.close();
      objectFileStream.close();
      outputStream.close();
      if (!downloadMD5.equals(awsMD5)) { /* Retry the download. */
        logger.warn(
                "First download attempt MD5 checksums for package " + key + " do not match.\n"
                        + "Calculated MD5 checksum for download: " + downloadMD5 + "\n"
                        + "MD5 checksum from Amazon S3 header:   " + awsMD5
        );
        object = getObject(key);
        outputStream = new FileOutputStream(outputPath);
        objectFileStream = object.getObjectContent();
        digestStream = new DigestInputStream(objectFileStream, md5);
        IOUtils.copy(digestStream, outputStream);
        downloadMD5 = Hex.encodeHexString(md5.digest());
        digestStream.close();
        objectFileStream.close();
        if (!downloadMD5.equals(awsMD5)) {
          /* MD5 checksums still don't match. Log an error and continue. */
          logger.warn(
                  "Second download attempt MD5 checksums for package " + key + " do not match.\n"
                          + "Calculated MD5 checksum for download: " + downloadMD5 + "\n"
                          + "MD5 checksum from Amazon S3 header:   " + awsMD5
          );
        }
      }
    } catch (IOException ex) {
      logger.error("Failed to open file stream with Amazon S3.", ex);
      throw ex;
    } catch (NullPointerException ex) {
      logger.error("Amazon S3 file stream is null", ex);
      throw ex;
    } catch (NoSuchAlgorithmException ex) {
      logger.warn("MD5 checksum generation is not supported.", ex);
    }

  }

  /**
   * Gets a file stream from an object in a S3 bucket.
   *
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
      logger.error("Amazon S3 file stream is null", ex);
      throw ex;
    }
    if (objectFileStream == null) {
      throw new NullPointerException("AWS file stream is null.");
    }
    return objectFileStream;
  }

}
