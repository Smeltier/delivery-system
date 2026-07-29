package br.com.delivery.infrastructure.web.dto;

import java.util.List;

import br.com.delivery.application.dto.order.FindClientOrdersOutput;

public record FindClientOrdersResponse(List<OrderResponse> orders) {

    public static FindClientOrdersResponse fromOutput(FindClientOrdersOutput output) {
        List<OrderResponse> orders = output.orders().stream()
                .map(OrderResponse::fromOutput)
                .toList();

        return new FindClientOrdersResponse(orders);
    }
}
