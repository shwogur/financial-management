package org.example.view;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

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
}