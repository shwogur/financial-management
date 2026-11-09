package org.example.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AccountCodeTest {

    @Test
    @DisplayName("앞자리 0인 코드 001을 그대로 유지한다")
    void keepsLeadingZero() {
        assertEquals("001", new AccountCode("001").value());
    }

    @ParameterizedTest
    @ValueSource(strings = {"1", "01", "1001", "abc", "10a", " 101", "-01", "１０１"})
    @DisplayName("3자리 ASCII 숫자가 아니면 거절한다")
    void rejectsInvalidFormat(String value) {
        assertThrows(IllegalArgumentException.class, () -> new AccountCode(value));
    }

    @Test
    @DisplayName("코드는 오름차순으로 비교된다")
    void comparesInAscendingOrder() {
        assertTrue(new AccountCode("001").compareTo(new AccountCode("101")) < 0);
    }
}