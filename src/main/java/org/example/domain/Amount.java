package org.example.domain;

public record Amount(long value) {

    private static final long MIN_VALUE = 1L;
    private static final long MAX_VALUE = 1_000_000_000L;

    public Amount {
        if (value < MIN_VALUE || value > MAX_VALUE) {
            throw new IllegalArgumentException("금액은 1원 이상 1,000,000,000원 이하여야 합니다.");
        }
    }
}
