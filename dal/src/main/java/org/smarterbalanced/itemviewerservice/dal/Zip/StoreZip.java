package org.smarterbalanced.itemviewerservice.dal.Zip;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;


import com.amazonaws.util.IOUtils;
import org.smarterbalanced.itemviewerservice.dal.AmazonApi.AmazonFileApi;
import org.smarterbalanced.itemviewerservice.dal.Redis.RedisConnection;
import redis.clients.jedis.JedisPool;


public class StoreZip {
  public List<String> UnpackToRedis(InputStream fileStream, JedisPool pool) {
    List<String> keys = new ArrayList<String>();
    ZipInputStream zipStream = new ZipInputStream(fileStream);
    ZipEntry entry;
    RedisConnection redis = new RedisConnection(pool);
    try {
      while ((entry = zipStream.getNextEntry()) != null) {
        byte[] fileData = new byte[500000000];
        zipStream.read(fileData);
        redis.storeByteFile(entry.getName(), fileData);
        System.out.println(entry.getName());
      }
    } catch (Exception e) {

    }
    return keys;
  }

  public static String unpackToBucket(InputStream fileStream, String fileName) {
    String bucketName = "sb-" + fileName;
    ZipInputStream zipStream = new ZipInputStream(fileStream);
    ZipEntry entry;
    AmazonFileApi amazonApi = new AmazonFileApi("bucketName");
    try {
      while ((entry = zipStream.getNextEntry()) != null) {
        byte[] fileData = new byte[500000000];
        zipStream.read(fileData);
        InputStream entryStream = new ByteArrayInputStream(fileData);
        amazonApi.storeFile( entryStream, entry.getName(), entry.getSize());
        System.out.println(entry.getName());
      }
    } catch (Exception e) {
        System.err.println("ERROR: Failed to stored file in Amazon bucket");
    }
    return bucketName;
  }

}
