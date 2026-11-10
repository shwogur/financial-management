package org.example.view.input;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.List;

public final class InputParser {

    private static final String DELIMITER = ",";
    private static final int KEEP_TRAILING_EMPTY_ITEMS = -1;

    private static final int MIN_YEAR = 1;
    private static final int MAX_YEAR = 9999;
    private static final String DATE_ERROR = "날짜는 yyyy-MM-dd 형식의 실제 존재하는 날짜여야 합니다. (예: 2026-11-06)";
    private static final String YEAR_RANGE_ERROR = "연도는 0001년부터 9999년까지 입력할 수 있습니다.";

    // 0, 또는 1~9로 시작하는 최대 18자리 ASCII 숫자 (부호·소수점·쉼표·공백·앞자리 0 거절, long 범위 보장)
    private static final String NUMBER_SHAPE = "0|[1-9][0-9]{0,17}";
    private static final String AMOUNT_ERROR = "금액은 부호, 소수점, 쉼표, 불필요한 앞자리 0 없이 숫자만 입력해야 합니다.";

    private static final int MIN_VOUCHER_NUMBER = 1;
    private static final String VOUCHER_NUMBER_ERROR = "전표번호는 1부터 2,147,483,647까지의 숫자만 입력해야 합니다.";

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
        LocalDate date = toDate(text);
        if (date.getYear() < MIN_YEAR || date.getYear() > MAX_YEAR) {
            throw new IllegalArgumentException(YEAR_RANGE_ERROR);
        }
        return date;
    }

    public static long parseAmount(String text) {
        if (!text.matches(NUMBER_SHAPE)) {
            throw new IllegalArgumentException(AMOUNT_ERROR);
        }
        return Long.parseLong(text);
    }

    public static int parseVoucherNumber(String text) {
        if (!text.matches(NUMBER_SHAPE)) {
            throw new IllegalArgumentException(VOUCHER_NUMBER_ERROR);
        }
        long number = Long.parseLong(text);
        if (number < MIN_VOUCHER_NUMBER || number > Integer.MAX_VALUE) {
            throw new IllegalArgumentException(VOUCHER_NUMBER_ERROR);
        }
        return (int) number;
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

    private static LocalDate toDate(String text) {
        try {
            // 기본 형식(ISO yyyy-MM-dd)은 엄격하게 해석하므로 2026-02-30, 2026-2-3을 거절한다.
            return LocalDate.parse(text);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException(DATE_ERROR, e);
        }
    }
}
