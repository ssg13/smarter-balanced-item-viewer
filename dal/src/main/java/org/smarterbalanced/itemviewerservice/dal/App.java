package org.smarterbalanced.itemviewerservice.dal;

import org.smarterbalanced.itemviewerservice.dal.AmazonApi.S3UpdateChecker;

public class App {

  public static void main( String[] args ) {
    System.out.println( "Hello World!" );
    String url = "";
    S3UpdateChecker checker = new S3UpdateChecker(url);
    checker.start();
    try{
      checker.join();
    } catch (Exception e) {
      System.exit(1);
    }

  }

}
