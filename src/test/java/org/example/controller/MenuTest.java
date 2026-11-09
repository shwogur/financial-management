package org.example.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MenuTest {

    @ParameterizedTest
    @CsvSource({
            "0,EXIT",
            "1,REGISTER_ACCOUNT",
            "2,SHOW_ACCOUNTS",
            "3,WRITE_VOUCHER",
            "4,CANCEL_VOUCHER",
            "5,SHOW_VOUCHERS",
            "6,SHOW_VOUCHER_DETAIL",
            "7,SHOW_BALANCE"
    })
    @DisplayName("0부터 7까지의 메뉴 번호를 메뉴로 변환한다")
    void from_validCommand(String input, Menu expected) {
        assertEquals(expected, Menu.from(input));
    }

    @ParameterizedTest
    @ValueSource(strings = {"8", "-1", "01", "a", "", " 1", "1 "})
    @DisplayName("0부터 7까지의 한 자리 숫자가 아니면 거절한다")
    void from_invalidCommand(String input) {
        assertThrows(IllegalArgumentException.class, () -> Menu.from(input));
    }
}