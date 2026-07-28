package br.com.delivery.domain.shared;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import br.com.delivery.domain.exception.InvalidCnpjValueException;

public class CnpjTest {
    @Test
    void shoudlThrowWhenValueIsNull() {
        String value = null;
        assertThrows(NullPointerException.class,
                () -> new Cnpj(value));
    }

    @Test
    void shoudlThrowWhenValueIsBlank() {
        String value = "";
        assertThrows(InvalidCnpjValueException.class,
                () -> new Cnpj(value));
    }

    @Test
    void shouldNotThrowWhenFormatIsValid() {
        assertDoesNotThrow(() -> new Cnpj("12.345.678/0001-95"));
    }

    @Test
    void shouldThrowWhenFormatIsInvalid() {
        String[] invalidCnpjFormats = {
            "12345678000195",
            "12.345.678/0001-9",
            "12.345.678/0001-195",
            "1.234.567/8000-19",
            "12.345.6780001-95",
            "12345.678/0001-95",
            "12/345/678-0001/95",
            "12-345-678/0001-95",
            " 12.345.678/0001-95",
            "12.345.678/0001-95 ",
            "12.345.678 /0001-95",
            "CNPJ: 12.345.678/0001-95",
            "12.345.6A8/0001-95"
        };

        for (String value : invalidCnpjFormats) {
            assertThrows(InvalidCnpjValueException.class,
                    () -> new Cnpj(value));
        }
    }
}
