package org.example.domain;

import java.util.Objects;

public class Account {

    private static final int MAX_NAME_LENGTH = 20;

    private final AccountCode code;
    private final String name;
    private final AccountType type;

    public Account(AccountCode code, String name, AccountType type) {
        this.code = Objects.requireNonNull(code);
        this.name = validateName(name);
        this.type = Objects.requireNonNull(type);
    }

    private static String validateName(String name) {
        String stripped = Objects.requireNonNull(name).strip();
        if (stripped.isEmpty() || stripped.length() > MAX_NAME_LENGTH) {
            throw new IllegalArgumentException("계정명은 1자 이상 20자 이하여야 합니다.");
        }
        return stripped;
    }

    public AccountCode code() {
        return code;
    }

    public String name() {
        return name;
    }

    public AccountType type() {
        return type;
    }
}