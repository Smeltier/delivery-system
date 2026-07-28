package br.com.delivery.application.dto.order;

import java.util.List;
import java.util.Objects;

import br.com.delivery.application.dto.order.OrderOutput;

public record FindClientOrdersOutput(List<OrderOutput> orders) {
}
