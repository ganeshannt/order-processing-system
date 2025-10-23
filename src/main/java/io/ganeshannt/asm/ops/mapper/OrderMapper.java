package io.ganeshannt.asm.ops.mapper;

import io.ganeshannt.asm.ops.dto.OrderItemDTO;
import io.ganeshannt.asm.ops.dto.OrderRequestDTO;
import io.ganeshannt.asm.ops.dto.OrderResponseDTO;
import io.ganeshannt.asm.ops.entity.Order;
import io.ganeshannt.asm.ops.entity.OrderItem;
import org.mapstruct.*;

import java.math.BigDecimal;
import java.util.List;


@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        injectionStrategy = InjectionStrategy.CONSTRUCTOR
)
public interface OrderMapper {

    @Mapping(target = "items", source = "items")
    OrderResponseDTO toResponseDTO(Order order);


    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "totalAmount", ignore = true)
    Order toEntity(OrderRequestDTO dto);


    @AfterMapping
    default void linkItemsAndCalculateTotal(@MappingTarget Order order) {
        if (order.getItems() != null) {
            // Set bidirectional relationship
            order.getItems().forEach(item -> item.setOrder(order));

            // Calculate total amount
            BigDecimal total = order.getItems().stream()
                    .map(OrderItem::getLineTotal)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            order.setTotalAmount(total);
        }
    }


    @Mapping(target = "lineTotal", expression = "java(item.getLineTotal())")
    OrderItemDTO toItemDTO(OrderItem item);


    @Mapping(target = "id", ignore = true)
    @Mapping(target = "order", ignore = true)
    OrderItem toItemEntity(OrderItemDTO dto);

    /**
     * Convert list of OrderItemDTOs to list of OrderItem entities
     */
    List<OrderItem> toItemEntityList(List<OrderItemDTO> dtos);
}
