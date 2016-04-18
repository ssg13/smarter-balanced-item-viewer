package org.smarterbalanced.itemviewerservice.dal;

import org.smarterbalanced.itemviewerservice.dal.AmazonApi.S3UpdateChecker;
import org.smarterbalanced.itemviewerservice.dal.AmazonApi.AmazonFileApi;
import org.smarterbalanced.itemviewerservice.dal.Zip.StoreZip;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

/**
 * The Application.
 */
public class App {

  /**
   * The entry point of application.
   *
   * @param args There are no input arguments needed.
   */
  public static void main( String[] args ) {
    System.out.println( "Hello World!" );
    String packageBucket = "cass-test";
    AmazonFileApi amazonApi = new AmazonFileApi(packageBucket);
    byte[] zip;
    try {
      zip = amazonApi.getS3File("IrpContentPackage.zip");
      InputStream stream = new ByteArrayInputStream(zip);
      StoreZip.unpackToBucket(stream, "test");

    } catch (Exception e) {
      System.out.println("Error");
      System.out.println(e.getMessage());

    }
  }

}
