package org.example.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class VoucherTest {

    private static final LocalDate DATE = LocalDate.of(2026, 11, 6);
    private static final Amount AMOUNT = new Amount(10_000L);
    private static final Account CASH = account("101", "현금", AccountType.ASSET);
    private static final Account SUPPLIES = account("501", "소모품비", AccountType.EXPENSE);

    private static Account account(String code, String name, AccountType type) {
        return new Account(new AccountCode(code), name, type);
    }

    private static Voucher normalWithDescription(String description) {
        return Voucher.normal(1, DATE, description, SUPPLIES, CASH, AMOUNT);
    }

    @Test
    @DisplayName("일반 전표는 구분 일반, 상태 정상으로 생성되고 입력한 내용을 그대로 가진다")
    void createsNormalVoucher() {
        Voucher voucher = Voucher.normal(1, DATE, "사무용품 구입", SUPPLIES, CASH, AMOUNT);

        assertAll(
                () -> assertEquals(1, voucher.number()),
                () -> assertEquals(DATE, voucher.date()),
                () -> assertEquals("사무용품 구입", voucher.description()),
                () -> assertEquals(SUPPLIES, voucher.debit()),
                () -> assertEquals(CASH, voucher.credit()),
                () -> assertEquals(AMOUNT, voucher.amount()),
                () -> assertEquals(VoucherType.NORMAL, voucher.type()),
                () -> assertEquals(VoucherStatus.NORMAL, voucher.status())
        );
    }

    @Test
    @DisplayName("적요의 앞뒤 공백은 제거하고 내부 공백은 유지한다")
    void stripsDescription() {
        assertEquals("운영자금 차입", normalWithDescription("  운영자금 차입  ").description());
    }

    @Test
    @DisplayName("적요 50자는 허용한다")
    void allowsMaxLengthDescription() {
        assertDoesNotThrow(() -> normalWithDescription("가".repeat(50)));
    }

    @Test
    @DisplayName("적요 51자는 거절한다")
    void rejectsTooLongDescription() {
        assertThrows(IllegalArgumentException.class, () -> normalWithDescription("가".repeat(51)));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   "})
    @DisplayName("빈 적요는 거절한다")
    void rejectsBlankDescription(String description) {
        assertThrows(IllegalArgumentException.class, () -> normalWithDescription(description));
    }

    @Test
    @DisplayName("차변과 대변에 같은 계정 코드를 선택하면 거절한다")
    void rejectsSameAccountCode() {
        assertThrows(IllegalArgumentException.class,
                () -> Voucher.normal(1, DATE, "잘못된 전표", CASH, CASH, AMOUNT));
    }

    @Test
    @DisplayName("분류가 같아도 서로 다른 계정이면 허용한다")
    void allowsDifferentAccountsOfSameType() {
        Account deposit = account("102", "보통예금", AccountType.ASSET);

        assertDoesNotThrow(() -> Voucher.normal(1, DATE, "현금 입금", deposit, CASH, AMOUNT));
    }

    @Test
    @DisplayName("계정명이 같아도 코드가 다르면 서로 다른 계정으로 허용한다")
    void allowsSameNameWithDifferentCode() {
        Account anotherCash = account("102", "현금", AccountType.ASSET);

        assertDoesNotThrow(() -> Voucher.normal(1, DATE, "현금 이동", anotherCash, CASH, AMOUNT));
    }
}
