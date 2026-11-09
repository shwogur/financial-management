package org.example.view;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.Arrays;
import java.util.List;

public final class InputParser {

    private static final String DELIMITER = ",";
    private static final int KEEP_TRAILING_EMPTY_ITEMS = -1;

    private static final String DATE_SHAPE = "[0-9]{4}-[0-9]{2}-[0-9]{2}";
    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("uuuu-MM-dd").withResolverStyle(ResolverStyle.STRICT);
    private static final int MIN_YEAR = 1;
    private static final String DATE_ERROR =
            "날짜는 0001-01-01부터 9999-12-31 사이의 실제 존재하는 yyyy-MM-dd 형식이어야 합니다.";

    private static final String NUMBER_SHAPE = "0|[1-9][0-9]{0,17}";
    private static final String AMOUNT_ERROR =
            "금액은 부호, 소수점, 쉼표, 불필요한 앞자리 0 없이 숫자만 입력해야 합니다.";

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

    public static LocalDate parseDate(String text) {
        if (!text.matches(DATE_SHAPE)) {
            throw new IllegalArgumentException(DATE_ERROR);
        }
        LocalDate date = toExistingDate(text);
        if (date.getYear() < MIN_YEAR) {
            throw new IllegalArgumentException(DATE_ERROR);
        }
        return date;
    }

    public static long parseAmount(String text) {
        if (!text.matches(NUMBER_SHAPE)) {
            throw new IllegalArgumentException(AMOUNT_ERROR);
        }
        return Long.parseLong(text);
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

    private static LocalDate toExistingDate(String text) {
        try {
            return LocalDate.parse(text, DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException(DATE_ERROR, e);
        }
    }
}
