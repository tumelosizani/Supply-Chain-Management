package dev.dini.scms.util.exception;

public class CustomerOrderNotFoundException extends RuntimeException {
    public CustomerOrderNotFoundException(Long id) {
        super("Customer order not found with id: " + id);
    }

}
