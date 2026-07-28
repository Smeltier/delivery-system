package br.com.delivery.application.dto.order;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import br.com.delivery.domain.account.AccountId;
import br.com.delivery.domain.order.OrderId;
import br.com.delivery.domain.order.OrderStatus;
import br.com.delivery.domain.restaurant.RestaurantId;
import br.com.delivery.domain.shared.Address;
import br.com.delivery.domain.shared.Money;

public record OrderOutput(
        OrderId id,
        RestaurantId restaurantId,
        AccountId accountId,
        OrderStatus status,
        List<OrderItemOutput> items,
        Address deliveryAddress,
        Money deliveryFee,
        Money total,
        LocalDateTime createdAt,
        Optional<LocalDateTime> paidAt,
        Optional<LocalDateTime> confirmedAt,
        Optional<LocalDateTime> deliveredAt,
        Optional<LocalDateTime> cancelledAt) {

    public OrderOutput {
        Objects.requireNonNull(id);
        Objects.requireNonNull(restaurantId);
        Objects.requireNonNull(accountId);
        Objects.requireNonNull(status);
        Objects.requireNonNull(items);
        Objects.requireNonNull(total);
        Objects.requireNonNull(createdAt);
    }
}