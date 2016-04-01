package org.smarterbalanced.itemviewerservice.dal;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.*;
import com.amazonaws.util.IOUtils;
import static java.lang.Math.toIntExact;

import java.io.IOException;
import java.io.InputStream;

public class AmazonApi {
    private String bucketName;
    private AmazonS3 s3connection;

    public AmazonApi(String bucketName) {
        this.bucketName = bucketName;
        this.s3connection = new AmazonS3Client();
    }

    public String getBucketName(){
        return this.bucketName;
    }

    public S3Object getObject (String key) {
        S3Object object = s3connection.getObject(new GetObjectRequest(this.bucketName, key));
        return object;
    }

    public byte[] getFile (S3Object object) {
        ObjectMetadata objectMetadata = object.getObjectMetadata();
        long awsFileSize = objectMetadata.getContentLength();
        int fileSize;
        try{
            fileSize = toIntExact(awsFileSize);
        } catch (ArithmeticException e) {
            //in this case the file size is > INT MAX. Or greater than ~2 GB.
            throw e;
        }

        byte[] fileData = new byte[fileSize];
        try {
            InputStream objectFileStream = object.getObjectContent();
            fileData = IOUtils.toByteArray(objectFileStream);
            objectFileStream.close();
        } catch (IOException ex) {
            //TODO: handle exception
            //occurs in case of IO errors
        } catch (NullPointerException ex) {
            //TODO: handle exception
            //occurs the case that the file stream fails to init.
        }
        return fileData;
    }
}
