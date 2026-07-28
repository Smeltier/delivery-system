package br.com.delivery.domain.shared;

import java.util.Objects;
import java.util.regex.Pattern;

import br.com.delivery.domain.exception.InvalidCnpjValueException;;

public record Cnpj(String value) {
    private static final Pattern CNPJ_PATTERN = Pattern.compile("^\\d{2}\\.\\d{3}\\.\\d{3}/\\d{4}-\\d{2}$");

    public Cnpj {
        value = Objects.requireNonNull(value, "CNPJ should be not null.");

        if (!CNPJ_PATTERN.matcher(value).matches()) {
            throw new InvalidCnpjValueException("Invalid CNPJ format.");
        }
    }
}
