package dev.dini.scms.order.service.calulation;

import dev.dini.scms.order.entity.CustomerOrder;

public interface OrderCalculationService {
    void calculateTotalCost(CustomerOrder order);
}
