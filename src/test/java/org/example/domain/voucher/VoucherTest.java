package org.example.domain.voucher;

import org.example.domain.account.Account;
import org.example.domain.account.AccountCode;
import org.example.domain.account.AccountType;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDate;
import java.util.OptionalInt;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
    @DisplayName("일반 전표는 구분 일반, 상태 정상, 원전표번호 없음으로 생성되고 입력한 내용을 그대로 가진다")
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
                () -> assertEquals(VoucherStatus.NORMAL, voucher.status()),
                () -> assertTrue(voucher.originalNumber().isEmpty())
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

    private static final LocalDate NEXT_DAY = DATE.plusDays(1);

    private static Voucher original() {
        return Voucher.normal(2, DATE, "사무용품 구입", SUPPLIES, CASH, AMOUNT);
    }

    @Test
    @DisplayName("취소하면 차변과 대변을 바꾸고 금액·적요를 복사한 취소 전표를 만든다")
    void createsReversalOnCancel() {
        Voucher reversal = original().cancel(4, NEXT_DAY);

        assertAll(
                () -> assertEquals(4, reversal.number()),
                () -> assertEquals(NEXT_DAY, reversal.date()),
                () -> assertEquals(CASH, reversal.debit()),
                () -> assertEquals(SUPPLIES, reversal.credit()),
                () -> assertEquals(AMOUNT, reversal.amount()),
                () -> assertEquals("사무용품 구입", reversal.description()),
                () -> assertEquals(VoucherType.CANCEL, reversal.type()),
                () -> assertEquals(VoucherStatus.NORMAL, reversal.status()),
                () -> assertEquals(OptionalInt.of(2), reversal.originalNumber())
        );
    }

    @Test
    @DisplayName("취소하면 원전표는 상태만 취소로 바뀌고 나머지 내용은 그대로다")
    void changesOnlyStatusOfOriginal() {
        Voucher original = original();

        original.cancel(4, NEXT_DAY);

        assertAll(
                () -> assertEquals(VoucherStatus.CANCELED, original.status()),
                () -> assertEquals(VoucherType.NORMAL, original.type()),
                () -> assertEquals(DATE, original.date()),
                () -> assertEquals(SUPPLIES, original.debit()),
                () -> assertEquals(CASH, original.credit()),
                () -> assertEquals(AMOUNT, original.amount()),
                () -> assertEquals("사무용품 구입", original.description()),
                () -> assertTrue(original.originalNumber().isEmpty())
        );
    }

    @Test
    @DisplayName("원전표 거래일자와 같은 날에도 취소할 수 있다")
    void allowsCancelOnSameDay() {
        assertDoesNotThrow(() -> original().cancel(4, DATE));
    }

    @Test
    @DisplayName("이미 취소된 전표는 다시 취소할 수 없다")
    void rejectsAlreadyCanceled() {
        Voucher original = original();
        original.cancel(4, NEXT_DAY);

        IllegalArgumentException exception =
                assertThrows(IllegalArgumentException.class, () -> original.cancel(5, NEXT_DAY));

        assertEquals("이미 취소된 전표입니다. (2)", exception.getMessage());
    }

    @Test
    @DisplayName("취소 전표 자체는 취소할 수 없다")
    void rejectsCancelingReversal() {
        Voucher reversal = original().cancel(4, NEXT_DAY);

        assertThrows(IllegalArgumentException.class, () -> reversal.cancel(5, NEXT_DAY));
        assertEquals(VoucherStatus.NORMAL, reversal.status());
    }

    @Test
    @DisplayName("원전표 거래일자보다 이른 취소일자는 거절하고 원전표 상태를 바꾸지 않는다")
    void rejectsEarlierCancelDateWithoutChangingStatus() {
        Voucher original = original();

        assertThrows(IllegalArgumentException.class, () -> original.cancel(4, DATE.minusDays(1)));
        assertEquals(VoucherStatus.NORMAL, original.status());
    }
}
