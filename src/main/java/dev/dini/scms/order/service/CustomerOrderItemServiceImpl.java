package dev.dini.scms.order.service;

import dev.dini.scms.order.dto.*;
import dev.dini.scms.order.entity.CustomerOrderItem;
import dev.dini.scms.util.exception.CustomerOrderNotFoundException;
import dev.dini.scms.order.mapper.CustomerOrderItemMapper;
import dev.dini.scms.order.repository.CustomerOrderItemRepository;
import dev.dini.scms.order.repository.CustomerOrderRepository;
import dev.dini.scms.product.service.ProductService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomerOrderItemServiceImpl implements CustomerOrderItemService {

    private final CustomerOrderItemRepository customerOrderItemRepository;
    private final CustomerOrderItemMapper orderItemMapper;
    private final CustomerOrderRepository customerOrderRepository;
    private final ProductService productService;

    @Override
    @Transactional
    public CustomerOrderItemResponseDTO createCustomerOrderItem(CustomerOrderItemRequestDTO createDTO) {
        log.info("Creating purchase order item {}", createDTO);
        CustomerOrderItem orderItem = orderItemMapper.toEntity(createDTO);

        orderItem.setCustomerOrder(customerOrderRepository.findById(createDTO.customerOrderId())
                .orElseThrow(() -> new CustomerOrderNotFoundException(createDTO.customerOrderId())));

        orderItem.setProduct(productService.getProductEntityById(createDTO.productId()));
        orderItem.setQuantity(createDTO.quantity());

        CustomerOrderItem saveCustomerOrderItem = customerOrderItemRepository.save(orderItem);
        log.info("Purchase order item created {}", saveCustomerOrderItem);
        return orderItemMapper.toResponseDTO(saveCustomerOrderItem);
    }

    @Override
    @Transactional
    public CustomerOrderItemResponseDTO updateCustomerOrderItem(Long id, CustomerOrderItemRequestDTO updateDTO) {
        log.info("Updating purchase order item {}", updateDTO);

        // Fetch the existing Customer order item
        CustomerOrderItem customerOrderItem = findCustomerOrderItemById(id);

        orderItemMapper.partialUpdate(updateDTO, customerOrderItem);
        CustomerOrderItem updatedPurchaseOrderItem = customerOrderItemRepository.save(customerOrderItem);
        log.info("Purchase order item updated {}", updatedPurchaseOrderItem);
        return orderItemMapper.toResponseDTO(updatedPurchaseOrderItem);
    }

    @Override
    public void deleteCustomerOrderItem(Long id) {
        customerOrderItemRepository.deleteById(id);
    }

    @Override
    public CustomerOrderItemResponseDTO getCustomerOrderItemById(Long id) {
        CustomerOrderItem customerOrderItem = findCustomerOrderItemById(id);
        return orderItemMapper.toResponseDTO(customerOrderItem);
    }

    @Override
    public CustomerOrderItem getEntityById(Long id) {
        return findCustomerOrderItemById(id);
    }

    private CustomerOrderItem findCustomerOrderItemById(Long id) {
        return customerOrderItemRepository.findById(id)
                .orElseThrow(() -> new CustomerOrderNotFoundException(id));
    }
}
