package org.example.domain.account;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AccountTypeTest {

    @ParameterizedTest
    @CsvSource({"자산,ASSET", "부채,LIABILITY", "수익,REVENUE", "비용,EXPENSE"})
    @DisplayName("네 가지 한글 분류명을 계정 분류로 변환한다")
    void from_validName(String name, AccountType expected) {
        assertEquals(expected, AccountType.from(name));
    }

    @ParameterizedTest
    @ValueSource(strings = {"자본", "", "ASSET"})
    @DisplayName("허용되지 않은 분류명은 거절한다")
    void from_invalidName(String name) {
        assertThrows(IllegalArgumentException.class, () -> AccountType.from(name));
    }
}