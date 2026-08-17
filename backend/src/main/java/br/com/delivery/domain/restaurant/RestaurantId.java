package br.com.delivery.domain.restaurant;

import java.util.Objects;
import java.util.UUID;

public record RestaurantId(UUID value) {
    public RestaurantId {
        Objects.requireNonNull(value);
    }

    public static RestaurantId generate() {
        return new RestaurantId(UUID.randomUUID());
    }
}
