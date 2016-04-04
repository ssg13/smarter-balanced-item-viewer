package org.smarterbalanced.itemviewerservice.dal;
//custom exception for files too large to fetch from Amazon api into memory.

public class FileTooLargeException extends Exception {

  public FileTooLargeException() {

  }

  public FileTooLargeException(String message) {
    super(message);
  }

  public FileTooLargeException(Throwable cause) {
    super(cause);
  }

  public FileTooLargeException(String message, Throwable cause) {
    super(message, cause);
  }
}
