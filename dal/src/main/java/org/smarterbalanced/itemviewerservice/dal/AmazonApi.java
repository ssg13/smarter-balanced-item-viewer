package org.smarterbalanced.itemviewerservice.dal;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.*;

public class AmazonApi {
    private String bucketName;
    private AmazonS3 s3connection;

    public AmazonApi(String bucketName) {
        this.bucketName = bucketName;
        this.s3connection = new AmazonS3Client();
    }
    public void setBucket(String bucketName){
        this.bucketName = bucketName;
    }
    public String getBucket(){
        return this.bucketName;
    }

    public ObjectListing objectsList() {
        ObjectListing objectListing = null;
        try {
            //TODO: request more objects if the first listObjects call does not return the full list
            objectListing = s3connection.listObjects(new ListObjectsRequest()
                    .withBucketName(this.bucketName));

        }
        catch (Exception e) {
            //TODO
        }
        return objectListing;
    }

    public S3Object getObject (String id) {
        S3Object object = null;
        try {
            object = s3connection.getObject(new GetObjectRequest(this.bucketName, id));
        }
        catch (Exception e) {
            //TODO
        }
        return object;
    }

}
