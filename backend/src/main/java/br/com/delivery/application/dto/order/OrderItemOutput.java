package br.com.delivery.application.dto.order;

import java.util.Objects;

import br.com.delivery.domain.restaurant.MenuItemCategory;
import br.com.delivery.domain.restaurant.MenuItemId;
import br.com.delivery.domain.shared.Money;

public record OrderItemOutput(
        MenuItemId menuItemId,
        String menuItemName,
        String description,
        MenuItemCategory category,
        int quantity,
        Money unitPrice,
        Money total) {

    public OrderItemOutput {
        Objects.requireNonNull(menuItemId);
        Objects.requireNonNull(menuItemName);
        Objects.requireNonNull(category);
        Objects.requireNonNull(unitPrice);
        Objects.requireNonNull(total);
    }
}