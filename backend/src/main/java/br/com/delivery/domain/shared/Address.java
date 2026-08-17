package br.com.delivery.domain.shared;

import java.util.Objects;

import br.com.delivery.domain.exception.InvalidAddressException;

public record Address(String street, String number, String complement, String city, String country, ZipCode zipCode) {
    public Address {
        Objects.requireNonNull(street);
        Objects.requireNonNull(number);
        Objects.requireNonNull(city);
        Objects.requireNonNull(country);
        Objects.requireNonNull(zipCode);

        if (street.isBlank() || number.isBlank() || city.isBlank() || country.isBlank()) {
            throw new InvalidAddressException("Campos obrigatórios do endereço não podem estar vazios.");
        }
    }
}
