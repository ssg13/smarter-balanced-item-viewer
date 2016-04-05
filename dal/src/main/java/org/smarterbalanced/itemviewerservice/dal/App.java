package org.smarterbalanced.itemviewerservice.dal;

import org.smarterbalanced.itemviewerservice.dal.AmazonApi.S3UpdateChecker;

public class App 
{
    public static void main( String[] args )
    {
        System.out.println( "Hello World!" );
        (new S3UpdateChecker("url")).start();
    }
}
