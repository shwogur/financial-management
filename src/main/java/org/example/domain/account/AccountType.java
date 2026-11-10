package org.example.domain.account;

import java.util.Arrays;

public enum AccountType {
    ASSET("자산"),
    LIABILITY("부채"),
    REVENUE("수익"),
    EXPENSE("비용");

    private final String displayName;

    AccountType(String displayName) {
        this.displayName = displayName;
    }

    public static AccountType from(String displayName) {
        return Arrays.stream(values())
                .filter(type -> type.displayName.equals(displayName))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "계정 분류는 자산, 부채, 수익, 비용 중 하나여야 합니다."));
    }

    public String displayName() {
        return displayName;
    }
}