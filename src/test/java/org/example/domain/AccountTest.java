package org.example.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AccountTest {

    private static final AccountCode CODE = new AccountCode("101");

    @Test
    @DisplayName("계정명의 앞뒤 공백을 제거한다")
    void stripsName() {
        Account account = new Account(CODE, "  현금  ", AccountType.ASSET);

        assertEquals("현금", account.name());
    }

    @Test
    @DisplayName("계정명 내부의 공백은 유지한다")
    void keepsInnerSpace() {
        Account account = new Account(CODE, "보통 예금", AccountType.ASSET);

        assertEquals("보통 예금", account.name());
    }

    @Test
    @DisplayName("계정명 20자는 허용한다")
    void allowsMaxLengthName() {
        assertDoesNotThrow(() -> new Account(CODE, "가".repeat(20), AccountType.ASSET));
    }

    @Test
    @DisplayName("계정명 21자는 거절한다")
    void rejectsTooLongName() {
        assertThrows(IllegalArgumentException.class,
                () -> new Account(CODE, "가".repeat(21), AccountType.ASSET));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   "})
    @DisplayName("빈 계정명은 거절한다")
    void rejectsBlankName(String name) {
        assertThrows(IllegalArgumentException.class,
                () -> new Account(CODE, name, AccountType.ASSET));
    }
}