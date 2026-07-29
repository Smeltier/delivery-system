package br.com.delivery.infrastructure.web.dto;

import java.math.BigDecimal;
import java.util.List;

import br.com.delivery.application.dto.order.OrderOutput;

public record OrderResponse(
    String orderId,
    String accountId,
    String restaurantId,
    String status,
    String createdAt,
    List<OrderItemResponse> items,
    BigDecimal total) {

    public static OrderResponse fromOutput(OrderOutput output) {
        List<OrderItemResponse> items = output.items().stream()
                .map(OrderItemResponse::fromOutput)
                .toList();

        return new OrderResponse(
                output.id().value().toString(),
                output.accountId().value().toString(),
                output.restaurantId().value().toString(),
                output.status().name(),
                output.createdAt().toString(),
                items,
                output.total().amount());
    }
}
