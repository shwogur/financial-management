package org.example.view;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class InputParserTest {

    private static final int ACCOUNT_ITEM_COUNT = 3;

    @Test
    @DisplayName("쉼표로 나눈 항목을 순서대로 반환한다")
    void splitsByComma() {
        List<String> items = InputParser.split("101,현금,자산", ACCOUNT_ITEM_COUNT);

        assertEquals(List.of("101", "현금", "자산"), items);
    }

    @Test
    @DisplayName("각 항목의 앞뒤 공백은 제거하고 내부 공백은 유지한다")
    void stripsEachItem() {
        List<String> items = InputParser.split(" 101 , 보통 예금 , 자산 ", ACCOUNT_ITEM_COUNT);

        assertEquals(List.of("101", "보통 예금", "자산"), items);
    }

    @ParameterizedTest
    @ValueSource(strings = {"101,현금", "101,현금,자산,부채"})
    @DisplayName("항목 개수가 부족하거나 초과하면 거절한다")
    void rejectsWrongCount(String line) {
        assertThrows(IllegalArgumentException.class,
                () -> InputParser.split(line, ACCOUNT_ITEM_COUNT));
    }

    @Test
    @DisplayName("마지막 쉼표가 있으면 거절한다")
    void rejectsTrailingComma() {
        assertThrows(IllegalArgumentException.class,
                () -> InputParser.split("101,현금,자산,", ACCOUNT_ITEM_COUNT));
    }

    @ParameterizedTest
    @ValueSource(strings = {"101,,자산", "101,   ,자산", ",현금,자산"})
    @DisplayName("빈 항목이 있으면 거절한다")
    void rejectsBlankItem(String line) {
        assertThrows(IllegalArgumentException.class,
                () -> InputParser.split(line, ACCOUNT_ITEM_COUNT));
    }

    @ParameterizedTest
    @CsvSource({
            "2026-11-06, 2026, 11, 6",
            "2024-02-29, 2024, 2, 29",
            "0001-01-01, 1, 1, 1",
            "9999-12-31, 9999, 12, 31"
    })
    @DisplayName("실제 존재하는 날짜를 변환한다 (윤년 2월 29일, 연도 경계 포함)")
    void parsesExistingDate(String text, int year, int month, int day) {
        assertEquals(LocalDate.of(year, month, day), InputParser.parseDate(text));
    }

    @ParameterizedTest
    @ValueSource(strings = {"2026-02-30", "2025-02-29", "2026-04-31", "2026-13-01", "2026-00-10", "2026-11-00"})
    @DisplayName("존재하지 않는 날짜는 거절한다")
    void rejectsNonExistingDate(String text) {
        assertThrows(IllegalArgumentException.class, () -> InputParser.parseDate(text));
    }

    @ParameterizedTest
    @ValueSource(strings = {"2026-2-3", "26-11-06", "2026/11/06", "20261106", "2026-11-06 ", "0000-01-01", "１２３４-11-06"})
    @DisplayName("yyyy-MM-dd 형식이 아니거나 연도가 0001 미만이면 거절한다")
    void rejectsWrongDateShape(String text) {
        assertThrows(IllegalArgumentException.class, () -> InputParser.parseDate(text));
    }

    @ParameterizedTest
    @CsvSource({"1, 1", "10000, 10000", "1000000000, 1000000000"})
    @DisplayName("숫자로만 된 금액을 변환한다")
    void parsesAmount(String text, long expected) {
        assertEquals(expected, InputParser.parseAmount(text));
    }

    @Test
    @DisplayName("0은 형식상 올바른 숫자이므로 변환하고, 범위 판단은 Amount에 맡긴다")
    void parsesZeroAmount() {
        assertEquals(0L, InputParser.parseAmount("0"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"-10000", "+100", "10.5", "10,000", "0100", "1 000", "abc", "１００", "1234567890123456789"})
    @DisplayName("부호, 소수점, 쉼표, 앞자리 0, 공백, 숫자가 아닌 문자가 있으면 거절한다")
    void rejectsWrongAmountShape(String text) {
        assertThrows(IllegalArgumentException.class, () -> InputParser.parseAmount(text));
    }
}