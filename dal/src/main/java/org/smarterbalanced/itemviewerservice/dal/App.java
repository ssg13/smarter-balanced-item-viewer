package org.smarterbalanced.itemviewerservice.dal;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.iterable.S3Objects;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import org.smarterbalanced.itemviewerservice.dal.AmazonApi.S3UpdateChecker;
import org.smarterbalanced.itemviewerservice.dal.AmazonApi.AmazonFileApi;

import java.io.FileOutputStream;

public class App 
{
    public static void main( String[] args )
    {
      System.out.println( "Starting:" );
      //(new S3UpdateChecker("url")).start();
      String amazonBucketName = "cass-test";
      AmazonFileApi bucket = new AmazonFileApi(amazonBucketName);

      bucket.listObjectKeys();
      byte[] file = null;
      try {
        file = bucket.getFile("IrpContentPackage.zip");
      } catch (Exception e) {
        System.err.println("error fetching file");
      }

      String path = "C:\\Users\\smithgar\\Downloads\\IrpContentPackage1.zip";
      try {
        FileOutputStream fp = new FileOutputStream(path);
        fp.write(file);
        fp.close();
      } catch (Exception e) {
        System.err.println("Failed to write file.");
      }


    }
}
