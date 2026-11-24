package dev.dini.scms.order.service.calulation;

import dev.dini.scms.order.entity.CustomerOrder;
import dev.dini.scms.order.entity.CustomerOrderItem;
import dev.dini.scms.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DefaultOrderCalculationService implements OrderCalculationService {

    private final ProductService productService;

    @Override
    public void calculateTotalCost(CustomerOrder order) {
        log.info("Calculating total cost for customer order {}", order);

        List<CustomerOrderItem> items = order.getItems();
        BigDecimal total = items.stream()
                .map(item -> getUnitPrice(item.getProduct().getId())
                        .multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        log.info("Total cost calculated: {}", total);
        order.setTotalCost(total);
    }

    private BigDecimal getUnitPrice(Long productId) {
        return productService.getEntityById(productId).getUnitPrice();
    }
}