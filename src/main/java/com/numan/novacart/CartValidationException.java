package com.numan.novacart;
import jakarta.ejb.ApplicationException;
/** Expected validation failure: preserve the Stateful EJB conversation. */
@ApplicationException(rollback = false)
public class CartValidationException extends IllegalArgumentException {
 private static final long serialVersionUID = 1L;
 public CartValidationException(String message) { super(message); }
}
