package org.example.view;

import java.util.Arrays;
import java.util.List;

public final class InputParser {

    private static final String DELIMITER = ",";
    private static final int KEEP_TRAILING_EMPTY_ITEMS = -1;

    private InputParser() {
    }

    public static List<String> split(String line, int expectedCount) {
        List<String> items = Arrays.stream(line.split(DELIMITER, KEEP_TRAILING_EMPTY_ITEMS))
                .map(String::strip)
                .toList();
        validateCount(items, expectedCount);
        validateNoBlank(items);
        return items;
    }

    private static void validateCount(List<String> items, int expectedCount) {
        if (items.size() != expectedCount) {
            throw new IllegalArgumentException("입력 항목은 " + expectedCount + "개여야 합니다.");
        }
    }

    private static void validateNoBlank(List<String> items) {
        if (items.stream().anyMatch(String::isEmpty)) {
            throw new IllegalArgumentException("빈 항목은 입력할 수 없습니다.");
        }
    }
}