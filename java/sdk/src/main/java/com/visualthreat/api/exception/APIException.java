package com.visualthreat.api.exception;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class APIException extends RuntimeException {
  public APIException(final String message) {
    super(message);
  }

  public APIException(final Throwable e) {
    super(e);
  }
}
