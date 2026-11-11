package org.example.domain.voucher;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EntrySideTest {

    @ParameterizedTest
    @CsvSource({"DEBIT, 차변", "CREDIT, 대변"})
    @DisplayName("기록 방향은 화면에 차변 또는 대변으로 표시된다")
    void displayName(EntrySide side, String expected) {
        assertEquals(expected, side.displayName());
    }
}
