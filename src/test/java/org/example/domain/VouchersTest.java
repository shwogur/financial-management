package org.example.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VouchersTest {

    private static final LocalDate DATE = LocalDate.of(2026, 11, 6);
    private static final Amount AMOUNT = new Amount(10_000L);
    private static final Account CASH = new Account(new AccountCode("101"), "현금", AccountType.ASSET);
    private static final Account LOAN = new Account(new AccountCode("201"), "차입금", AccountType.LIABILITY);

    private Vouchers vouchers;

    @BeforeEach
    void setUp() {
        vouchers = new Vouchers();
    }

    private Voucher writeValid() {
        return vouchers.write(DATE, "운영자금 차입", CASH, LOAN, AMOUNT);
    }

    @Test
    @DisplayName("처음에는 작성된 전표가 없다")
    void emptyAtStart() {
        assertTrue(vouchers.findAll().isEmpty());
    }

    @Test
    @DisplayName("전표번호는 1부터 시작해 작성 순서대로 1씩 증가한다")
    void numbersConsecutivelyFromOne() {
        assertEquals(1, writeValid().number());
        assertEquals(2, writeValid().number());
        assertEquals(3, writeValid().number());
    }

    @Test
    @DisplayName("작성한 전표를 번호 순서대로 보관한다")
    void keepsVouchersInNumberOrder() {
        Voucher first = writeValid();
        Voucher second = vouchers.write(DATE, "운영자금 상환", LOAN, CASH, AMOUNT);

        assertEquals(List.of(first, second), vouchers.findAll());
    }

    @Test
    @DisplayName("작성에 실패하면 전표를 남기지 않고 다음 전표번호도 소비하지 않는다")
    void failedWriteDoesNotConsumeNumber() {
        writeValid();

        assertThrows(IllegalArgumentException.class,
                () -> vouchers.write(DATE, "같은 계정", CASH, CASH, AMOUNT));
        assertThrows(IllegalArgumentException.class,
                () -> vouchers.write(DATE, "   ", CASH, LOAN, AMOUNT));

        assertEquals(1, vouchers.findAll().size());
        assertEquals(2, writeValid().number());
    }

    @Test
    @DisplayName("첫 작성이 실패해도 다음 정상 전표는 1번이다")
    void firstFailureKeepsNumberOne() {
        assertThrows(IllegalArgumentException.class,
                () -> vouchers.write(DATE, "같은 계정", CASH, CASH, AMOUNT));

        assertEquals(1, writeValid().number());
    }

    @Test
    @DisplayName("전표 목록마다 번호를 따로 매긴다")
    void eachVouchersStartsFromOne() {
        writeValid();
        writeValid();

        Vouchers another = new Vouchers();

        assertEquals(1, another.write(DATE, "다른 장부", CASH, LOAN, AMOUNT).number());
    }

    @Test
    @DisplayName("조회 결과로 내부 목록을 수정할 수 없다")
    void findAllIsUnmodifiable() {
        writeValid();
        List<Voucher> all = vouchers.findAll();

        assertThrows(UnsupportedOperationException.class, all::clear);
    }

    @Test
    @DisplayName("전표번호로 해당 전표를 찾는다")
    void findsVoucherByNumber() {
        writeValid();
        Voucher second = vouchers.write(DATE, "운영자금 상환", LOAN, CASH, AMOUNT);
        writeValid();

        assertEquals(second, vouchers.findByNumber(2));
    }

    @Test
    @DisplayName("존재하지 않는 전표번호로 찾으면 거절한다")
    void rejectsUnknownNumber() {
        writeValid();
        writeValid();

        assertThrows(IllegalArgumentException.class, () -> vouchers.findByNumber(3));
    }

    @Test
    @DisplayName("작성에 실패한 요청의 번호로는 전표를 찾을 수 없다")
    void cannotFindNumberOfFailedWrite() {
        assertThrows(IllegalArgumentException.class,
                () -> vouchers.write(DATE, "같은 계정", CASH, CASH, AMOUNT));

        assertThrows(IllegalArgumentException.class, () -> vouchers.findByNumber(1));
    }

    @Test
    @DisplayName("전표가 있는지 알려준다")
    void tellsWhetherEmpty() {
        assertTrue(vouchers.isEmpty());

        writeValid();

        assertFalse(vouchers.isEmpty());
    }
}
