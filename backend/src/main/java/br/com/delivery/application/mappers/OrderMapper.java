package br.com.delivery.application.mappers;

import java.util.List;

import br.com.delivery.application.dto.order.OrderOutput;
import br.com.delivery.application.dto.order.OrderItemOutput;
import br.com.delivery.domain.order.Order;

public final class OrderMapper {
    private OrderMapper() {}

    public static OrderOutput toOutput(Order order) {
        List<OrderItemOutput> items = order.getItems().stream()
                .map(item -> new OrderItemOutput(
                        item.getMenuItemId(),
                        item.getMenuItemName(),
                        item.getMenuItemDescription(),
                        item.getMenuItemCategory(),
                        item.getQuantity(),
                        item.getUnitPrice(),
                        item.total()))
                .toList();

        return new OrderOutput(
                order.getId(),
                order.getRestaurantId(),
                order.getAccountId(),
                order.getStatus(),
                items,
                order.getDeliveryAddress(),
                order.getDeliveryFee(),
                order.total(),
                order.getCreatedAt(),
                order.getPaidAt(),
                order.getConfirmedAt(),
                order.getDeliveredAt(),
                order.getCancelledAt());
    }
}
