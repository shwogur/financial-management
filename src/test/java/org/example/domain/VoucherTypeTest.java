package org.example.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

class VoucherTypeTest {

    @ParameterizedTest
    @CsvSource({"NORMAL, 일반", "CANCEL, 취소"})
    @DisplayName("전표 구분은 화면에 일반 또는 취소로 표시된다")
    void displayName(VoucherType type, String expected) {
        assertEquals(expected, type.displayName());
    }
}
