package br.com.delivery.domain.shared;

import java.util.Objects;
import java.math.BigDecimal;

import br.com.delivery.domain.exception.InvalidMoneyException;
import br.com.delivery.domain.exception.CurrencyMismatchException;
import br.com.delivery.domain.exception.InsufficientFundsException;

public record Money(BigDecimal amount, Currency currency) {
    public Money {
        Objects.requireNonNull(amount);
        Objects.requireNonNull(currency);

        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new InvalidMoneyException("A quantidade não pode ser negativa.");
        }
    }

    public static Money of(double amount, Currency currency) {
        return new Money(BigDecimal.valueOf(amount), currency);
    }

    public static Money of(BigDecimal amount, Currency currency) {
        return new Money(amount, currency);
    }

    public static Money zero(Currency currency) {
        return new Money(BigDecimal.ZERO, currency);
    }

    public Money add(Money value) {
        ensureSameCurrency(value);
        return new Money(this.amount.add(value.amount), this.currency);
    }

    public Money subtract(Money value) {
        ensureSameCurrency(value);
        if (this.amount.compareTo(value.amount) < 0) {
            throw new InsufficientFundsException("Saldo insuficiente.");
        }

        return new Money(this.amount.subtract(value.amount), this.currency);
    }

    public Money multiply(int factor) {
        if (factor < 0) {
            throw new InvalidMoneyException("O fator não pode ser negativo.");
        }

        return new Money(this.amount.multiply(BigDecimal.valueOf(factor)), this.currency);
    }

    public boolean isPositive() {
        return amount.compareTo(BigDecimal.ZERO) > 0;
    }

    public boolean isZero() {
        return amount.compareTo(BigDecimal.ZERO) == 0;
    }

    private void ensureSameCurrency(Money value) {
        if (!this.currency.equals(value.currency)) {
            throw new CurrencyMismatchException("Moedas diferentes.");
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof Money money)) {
            return false;
        }

        return amount.compareTo(money.amount) == 0 && currency == money.currency;
    }

    @Override
    public int hashCode() {
        return Objects.hash(amount.stripTrailingZeros(), currency);
    }
}
