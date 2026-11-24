package dev.dini.scms.order.event;

/**
 * Domain event published when a customer order transitions to {@code CONFIRMED}.
 */
public record OrderConfirmedEvent(Long orderId) {
}
