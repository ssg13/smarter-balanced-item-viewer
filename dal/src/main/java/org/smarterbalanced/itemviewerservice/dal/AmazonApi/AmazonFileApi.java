package org.smarterbalanced.itemviewerservice.dal.AmazonApi;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CreateBucketRequest;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.amazonaws.util.IOUtils;

import org.apache.commons.codec.binary.Hex;
import org.smarterbalanced.itemviewerservice.dal.Exceptions.FileTooLargeException;

import java.io.IOException;
import java.io.InputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;


public class AmazonFileApi {
  private static final Logger log = Logger.getLogger("org.smarterbalanced.dal");
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
    this.s3connection.setEndpoint("s3-us-west-2.amazonaws.com");
  }

  private S3Object getObject(String key) {
    S3Object object = s3connection.getObject(new GetObjectRequest(this.bucketName, key));
    return object;
  }

  private void createBucket(String bucketName) {
    try {
      if (!(this.s3connection.doesBucketExist(bucketName))) {
        this.s3connection.createBucket(new CreateBucketRequest(bucketName));
      }

    } catch (AmazonServiceException ase) {
      log.log(Level.SEVERE, ase.toString(), ase);
      throw ase;
    } catch (AmazonClientException ace) {
      log.log(Level.SEVERE, ace.toString(), ace);
      throw ace;
    }
  }

  /**
   * Lists all objects in the S3 bucket.
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
      log.log(Level.SEVERE, "Amazon rejected the connection to S3.", ase);
      throw ase;
    } catch (AmazonClientException ace) {
      log.log(Level.SEVERE, "Network error connecting to Amazon S3.");
      throw ace;
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
      awsMD5 = awsMD5.replaceAll("\\s+",""); /* Strip any extra whitespace. */
      if (awsMD5.length() != 32) {
        log.log(Level.WARNING, "MD5 metadata for package " + key + " is not 32 characters.");
      }
      md5 = MessageDigest.getInstance("MD5");
      objectFileStream = object.getObjectContent();
      DigestInputStream digestStream = new DigestInputStream(objectFileStream, md5);
      fileData = IOUtils.toByteArray(digestStream);
      downloadMD5 = Hex.encodeHexString(md5.digest());
      digestStream.close();
      objectFileStream.close();
      if (!downloadMD5.equals(awsMD5)) { /* Retry the download. */
        log.log(Level.INFO,
                "First download attempt MD5 checksums for package " + key + " do not match.");
        object = getObject(key);
        objectFileStream = object.getObjectContent();
        digestStream = new DigestInputStream(objectFileStream, md5);
        fileData = IOUtils.toByteArray(digestStream);
        downloadMD5 = Hex.encodeHexString(md5.digest());
        digestStream.close();
        objectFileStream.close();
        if (!downloadMD5.equals(awsMD5)) {
          /* MD5 checksums still don't match. Log an error and continue. */
          log.log(Level.WARNING,
                  "Second download attempt MD5 checksums for package " + key + " do not match.");
        }
      }
      System.out.println("Zip MD5: " + downloadMD5);
      System.out.println("AWS MD5: " + awsMD5);
    } catch (IOException ex) {
      log.log(Level.SEVERE, "Failed to open file stream with Amazon S3.", ex);
      throw ex;
    } catch (NullPointerException ex) {
      log.log(Level.SEVERE, "Amazon S3 file stream is null", ex);
      throw ex;
    } catch (NoSuchAlgorithmException ex) {
      log.log(Level.SEVERE, "MD5 checksum generation is not supported.", ex);
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
      log.log(Level.SEVERE, "Amazon S3 file stream is null", ex);
      throw ex;
    }
    if (objectFileStream == null) {
      throw new NullPointerException("AWS file stream is null.");
    }
    return objectFileStream;
  }

  /**
   * Store file.
   *
   * @param fileStream Input stream for the ZipFile
   * @param key        Key to use when storing the file
   * @param size       File size in bytes
   */
  public void storeFile(InputStream fileStream, String key, long size) {
    ObjectMetadata objectData = new ObjectMetadata();
    objectData.setContentLength(size);
    try {
      this.s3connection.putObject(new PutObjectRequest(
              this.bucketName, key, fileStream, objectData));
    } catch (AmazonServiceException ase) {
      log.log(Level.SEVERE, "Amazon rejected putObject request.", ase);
      throw ase;
    } catch (AmazonClientException ace) {
      log.log(Level.SEVERE, "Network error connecting to Amazon S3.", ace);
      throw ace;
    }
  }

}
