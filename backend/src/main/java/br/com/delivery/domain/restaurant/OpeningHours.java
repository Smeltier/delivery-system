package br.com.delivery.domain.restaurant;

import java.time.LocalTime;
import java.util.Objects;

public record OpeningHours(LocalTime open, LocalTime close) {
    public OpeningHours {
        Objects.requireNonNull(open);
        Objects.requireNonNull(close);

        if (close.isBefore(open)) {
            throw new IllegalArgumentException("Horário de fechamento não pode ser antes do de abertura.");
        }
    }

    public boolean isWithin(LocalTime time) {
        return !time.isBefore(open) && !time.isAfter(close);
    }
}
