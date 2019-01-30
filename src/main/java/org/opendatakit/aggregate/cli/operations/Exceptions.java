package org.opendatakit.aggregate.cli.operations;

public final class Exceptions {
  public static final class OperationException extends RuntimeException {
    public OperationException() {
    }

    public OperationException(String message) {
      super(message);
    }

    public OperationException(String message, Throwable cause) {
      super(message, cause);
    }

    public OperationException(Throwable cause) {
      super(cause);
    }

    public OperationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
      super(message, cause, enableSuppression, writableStackTrace);
    }
  }

  public static final class ConfigurationException extends RuntimeException {
    public ConfigurationException() {
    }

    public ConfigurationException(String message) {
      super(message);
    }

    public ConfigurationException(String message, Throwable cause) {
      super(message, cause);
    }

    public ConfigurationException(Throwable cause) {
      super(cause);
    }

    public ConfigurationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
      super(message, cause, enableSuppression, writableStackTrace);
    }
  }
}
