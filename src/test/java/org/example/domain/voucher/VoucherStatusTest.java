package org.example.domain.voucher;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

class VoucherStatusTest {

    @ParameterizedTest
    @CsvSource({"NORMAL, 정상", "CANCELED, 취소"})
    @DisplayName("전표 상태는 화면에 정상 또는 취소로 표시된다")
    void displayName(VoucherStatus status, String expected) {
        assertEquals(expected, status.displayName());
    }
}
