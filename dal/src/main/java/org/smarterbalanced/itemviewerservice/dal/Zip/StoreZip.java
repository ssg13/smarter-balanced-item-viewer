package org.smarterbalanced.itemviewerservice.dal.Zip;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.smarterbalanced.itemviewerservice.dal.AmazonApi.AmazonFileApi;


public class StoreZip {
  public List<String> UnpackToRedis(InputStream fileStream) {
    List<String> keys = new ArrayList<String>();
    ZipInputStream zipStream = new ZipInputStream(fileStream);
    ZipEntry entry;
    try {
      while ((entry = zipStream.getNextEntry()) != null) {
      }
    } catch (IOException e) {

    }

    return keys;
  }

  public String UnpackToBucket(InputStream fileStream, String fileName) {
    String bucketName = "sb-" + fileName;
    ZipInputStream zipStream = new ZipInputStream(fileStream);
    ZipEntry entry;
    AmazonFileApi amazonApi = new AmazonFileApi("bucketName");
    try {
      while ((entry = zipStream.getNextEntry()) != null) {

      }
    } catch (IOException e) {

    }

    return bucketName;
  }

}
