package org.smarterbalanced.itemviewerservice.dal.Exceptions;

public class RedisFileException extends Exception {
  public RedisFileException() {}

  public RedisFileException(String message) {
    super(message);
  }

  public RedisFileException(Throwable cause) {
    super(cause);
  }

  public RedisFileException(String message, Throwable cause) {
    super(message, cause);
  }
}
