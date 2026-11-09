package org.example.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AmountTest {

    @ParameterizedTest
    @ValueSource(longs = {1L, 1_000_000_000L})
    @DisplayName("1원과 1,000,000,000원은 허용한다")
    void allowsBoundaryValues(long value) {
        assertEquals(value, new Amount(value).value());
    }

    @ParameterizedTest
    @ValueSource(longs = {0L, -1L, -10_000L, 1_000_000_001L, Long.MAX_VALUE})
    @DisplayName("0원, 음수, 1,000,000,000원 초과는 거절한다")
    void rejectsOutOfRange(long value) {
        assertThrows(IllegalArgumentException.class, () -> new Amount(value));
    }

    @Test
    @DisplayName("금액이 같으면 같은 금액으로 취급한다")
    void equalsByValue() {
        assertEquals(new Amount(10_000L), new Amount(10_000L));
    }
}
