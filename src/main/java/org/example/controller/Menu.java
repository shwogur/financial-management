package org.example.controller;

import java.util.Arrays;

public enum Menu {
    EXIT("0"),
    REGISTER_ACCOUNT("1"),
    SHOW_ACCOUNTS("2"),
    WRITE_VOUCHER("3"),
    CANCEL_VOUCHER("4"),
    SHOW_VOUCHERS("5"),
    SHOW_VOUCHER_DETAIL("6"),
    SHOW_BALANCE("7");

    private final String command;

    Menu(String command) {
        this.command = command;
    }

    public static Menu from(String input) {
        return Arrays.stream(values())
                .filter(menu -> menu.command.equals(input))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "메뉴는 0부터 7까지의 숫자 중 하나여야 합니다."));
    }
}