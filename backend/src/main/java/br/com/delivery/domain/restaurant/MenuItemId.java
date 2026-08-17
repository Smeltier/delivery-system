package br.com.delivery.domain.restaurant;

import java.util.Objects;
import java.util.UUID;

public record MenuItemId(UUID value) {
    public MenuItemId {
        Objects.requireNonNull(value);
    }

    public static MenuItemId generate() {
        return new MenuItemId(UUID.randomUUID());
    }
}
