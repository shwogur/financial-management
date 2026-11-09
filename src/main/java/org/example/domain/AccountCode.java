package org.example.domain;

public record AccountCode(String value) implements Comparable<AccountCode> {

    private static final String THREE_ASCII_DIGITS = "[0-9]{3}";

    public AccountCode {
        if (value == null || !value.matches(THREE_ASCII_DIGITS)) {
            throw new IllegalArgumentException("계정 코드는 3자리 숫자여야 합니다.");
        }
    }

    @Override
    public int compareTo(AccountCode other) {
        return value.compareTo(other.value);
    }
}