package br.com.delivery.domain.shared;

import java.util.regex.Pattern;

public record ZipCode(String value) {
    private static final Pattern BRAZIL_ZIP_PATTERN = Pattern.compile("\\d{5}-?\\d{3}");

    public ZipCode {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("CEP inválido.");
        }

        value = value.trim();

        if (!BRAZIL_ZIP_PATTERN.matcher(value).matches()) {
            throw new IllegalArgumentException("CEP inválido.");
        }

        value = normalize(value);
    }

    private String normalize(String value) {
        return value.replace("-", "");
    }

    @Override
    public String toString() {
        return value.substring(0, 5) + "-" + value.substring(5);
    }
}
