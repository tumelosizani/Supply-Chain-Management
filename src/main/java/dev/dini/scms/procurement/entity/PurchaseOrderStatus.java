package dev.dini.scms.procurement.entity;

import lombok.Getter;

@Getter
public enum PurchaseOrderStatus {
    PENDING("Pending approval"),
    APPROVED("Approved"),
    SHIPPED("Shipped"),
    DELIVERED("Delivered"),
    PARTIALLY_RECEIVED("Partially Received"),
    FULLY_RECEIVED("Fully Received"),
    CANCELLED("Cancelled"),
    RETURNED("Returned");

    private final String description;

    PurchaseOrderStatus(String description) {
        this.description = description;
    }

    public boolean canReceiveGoods() {
        return this == APPROVED || this == SHIPPED || this == DELIVERED || this == PARTIALLY_RECEIVED;
    }

    public boolean isTerminal() {
        return this == FULLY_RECEIVED || this == CANCELLED || this == RETURNED;
    }
}
