package dev.dini.scms.order.service;

import dev.dini.scms.order.dto.*;
import dev.dini.scms.order.entity.CustomerOrderItem;

public interface CustomerOrderItemService {
    CustomerOrderItemResponseDTO createCustomerOrderItem(CustomerOrderItemRequestDTO createDTO);

    CustomerOrderItemResponseDTO updateCustomerOrderItem(Long id, CustomerOrderItemRequestDTO updateDTO);

    void deleteCustomerOrderItem(Long id);

    CustomerOrderItemResponseDTO getCustomerOrderItemById(Long id);

    CustomerOrderItem getEntityById(Long id);

}
