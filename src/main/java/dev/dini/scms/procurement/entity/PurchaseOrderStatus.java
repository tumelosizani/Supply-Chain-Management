package dev.dini.scms.procurement.entity;

import lombok.*;

@Getter
public enum PurchaseOrderStatus {
    PENDING("Pending approval"),
    APPROVED("Approved"),
    SHIPPED("Shipped"),
    DELIVERED("Delivered"),
    CANCELLED("Cancelled"),
    RETURNED("Returned");


    private final String description;

    PurchaseOrderStatus(String description) {
        this.description = description;
    }

}
