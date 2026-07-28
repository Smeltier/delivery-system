package br.com.delivery.domain.shared;

import java.util.Objects;
import java.util.regex.Pattern;

import br.com.delivery.domain.exception.InvalidCpfValueException;

public record Cpf(String value) {
    private static final Pattern CPF_PATTERN = Pattern.compile("^(\\d{3}[.]\\d{3}[.]\\d{3}-\\d{2}|\\d{11})$");

    public Cpf {
        Objects.requireNonNull(value);

        if (!CPF_PATTERN.matcher(value).matches()) {
            throw new InvalidCpfValueException("Formato de CPF inválido.");
        }

        String cleanValue = value.replaceAll("\\D", "");

        if (isSequence(cleanValue) || !isValidCpf(cleanValue)) {
            throw new InvalidCpfValueException("Número de CPF inválido.");
        }

        value = cleanValue;
    }

    private boolean isSequence(String cpf) {
        return cpf.matches("(\\d)\\1{10}");
    }

    private boolean isValidCpf(String cpf) {
        return calculateDigit(cpf.substring(0, 9), 10) == Character.getNumericValue(cpf.charAt(9))
            && calculateDigit(cpf.substring(0, 10), 11) == Character.getNumericValue(cpf.charAt(10));
    }

    private int calculateDigit(String str, int weight) {
        int sum = 0;

        for (char c : str.toCharArray()) {
            sum += Character.getNumericValue(c) * weight--;
        }

        int remainder = sum % 11;

        return (remainder < 2) ? 0 : 11 - remainder;
    }
}
