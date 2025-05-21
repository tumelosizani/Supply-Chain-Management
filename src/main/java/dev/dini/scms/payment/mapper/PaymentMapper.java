package dev.dini.scms.payment.mapper;

import dev.dini.scms.payment.dto.PaymentRequestDTO;
import dev.dini.scms.payment.dto.PaymentResponseDTO;
import dev.dini.scms.payment.entity.Payment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PaymentMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "paymentDate", ignore = true)
    Payment toEntity(PaymentRequestDTO paymentRequestDTO);


    @Mapping(target = "orderId", source = "customerOrder.id")
    PaymentResponseDTO toResponseDTO(Payment savedPayment);
}
