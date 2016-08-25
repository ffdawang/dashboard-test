package com.wsn.board.bs.util;

class FileUtilitiesException extends RuntimeException
{
  public FileUtilitiesException()
  {
  }

  public FileUtilitiesException(String message)
  {
    super(message);
  }

  public FileUtilitiesException(Throwable cause) {
    super(cause);
  }

  public FileUtilitiesException(String message, Throwable cause) {
    super(message, cause);
  }
}