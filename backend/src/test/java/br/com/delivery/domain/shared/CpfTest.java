package br.com.delivery.domain.shared;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import br.com.delivery.domain.exception.InvalidCpfValueException;

public class CpfTest {
    @Test
    void shouldNormalizeValue() {
        String value = "955.999.370-46";
        String normalizedValue = "95599937046";

        var cpf = new Cpf(value);

        assertEquals(normalizedValue, cpf.value());
    }

    @Test
    void shouldThrowWhenValueIsASequence() {
        String value = "111.11.111-11";

        assertThrows(InvalidCpfValueException.class,
                () -> new Cpf(value));
    }

    @Test
    void shouldThrowWhenValueIsBlank() {
        String value = "";

        assertThrows(InvalidCpfValueException.class,
                () -> new Cpf(value));
    }

    @Test
    void shouldThrowWhenValueDontMatchWithPattern() {
        String value = "955.999-370.46";

        assertThrows(InvalidCpfValueException.class,
                () -> new Cpf(value));
    }
}
