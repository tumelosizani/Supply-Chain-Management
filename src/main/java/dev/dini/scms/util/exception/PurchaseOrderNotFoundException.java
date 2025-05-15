package dev.dini.scms.util.exception;

public class PurchaseOrderNotFoundException extends RuntimeException {
    public PurchaseOrderNotFoundException(Long id) {
        super("Purchase order not found with id: " + id);
    }


}
