package br.com.delivery.infrastructure.web.dto;

import java.math.BigDecimal;

import br.com.delivery.application.dto.order.OrderItemOutput;

public record OrderItemResponse(
        String menuItemId,
        String menuItemName,
        String description,
        String category,
        int quantity,
        BigDecimal unitPrice,
        BigDecimal total) {

    public static OrderItemResponse fromOutput(OrderItemOutput output) {
        return new OrderItemResponse(
                output.menuItemId().value().toString(),
                output.menuItemName(),
                output.description(),
                output.category().name(),
                output.quantity(),
                output.unitPrice().amount(),
                output.total().amount());
    }
}
