package dev.dini.scms.util.exception;

public class InvalidPurchaseOrderStateException extends RuntimeException {
    public InvalidPurchaseOrderStateException(String message) {
        super(message);
    }
}
