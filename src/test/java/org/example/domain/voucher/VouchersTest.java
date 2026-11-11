package org.example.domain.voucher;

import org.example.domain.account.Account;
import org.example.domain.account.AccountCode;
import org.example.domain.account.AccountType;

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

    private static final LocalDate NEXT_DAY = DATE.plusDays(1);

    private void writeThree() {
        writeValid();
        writeValid();
        writeValid();
    }

    @Test
    @DisplayName("취소하면 다음 번호의 취소 전표를 저장하고 원전표를 취소 상태로 바꾼다")
    void cancelsWithNextNumber() {
        writeThree();

        Voucher reversal = vouchers.cancel(2, NEXT_DAY);

        assertEquals(4, reversal.number());
        assertEquals(4, vouchers.findAll().size());
        assertEquals(reversal, vouchers.findByNumber(4));
        assertEquals(VoucherStatus.CANCELED, vouchers.findByNumber(2).status());
    }

    @Test
    @DisplayName("일반 전표와 취소 전표는 같은 번호 순서를 사용한다")
    void sharesNumberSequenceWithReversal() {
        writeThree();
        vouchers.cancel(2, NEXT_DAY);

        assertEquals(5, writeValid().number());
    }

    @Test
    @DisplayName("없는 번호를 취소하면 거절하고 전표 수와 다음 번호를 바꾸지 않는다")
    void rejectsUnknownNumberWithoutChange() {
        writeThree();

        assertThrows(IllegalArgumentException.class, () -> vouchers.cancel(9, NEXT_DAY));

        assertEquals(3, vouchers.findAll().size());
        assertEquals(4, writeValid().number());
    }

    @Test
    @DisplayName("이미 취소한 전표를 다시 취소하면 거절하고 전표 수와 다음 번호를 바꾸지 않는다")
    void rejectsDuplicateCancelWithoutChange() {
        writeThree();
        vouchers.cancel(2, NEXT_DAY);

        assertThrows(IllegalArgumentException.class, () -> vouchers.cancel(2, NEXT_DAY));

        assertEquals(4, vouchers.findAll().size());
        assertEquals(5, writeValid().number());
    }

    @Test
    @DisplayName("취소 전표를 취소하면 거절하고 취소 전표 상태와 다음 번호를 바꾸지 않는다")
    void rejectsCancelingReversalWithoutChange() {
        writeThree();
        vouchers.cancel(2, NEXT_DAY);

        assertThrows(IllegalArgumentException.class, () -> vouchers.cancel(4, NEXT_DAY));

        assertEquals(VoucherStatus.NORMAL, vouchers.findByNumber(4).status());
        assertEquals(4, vouchers.findAll().size());
        assertEquals(5, writeValid().number());
    }

    @Test
    @DisplayName("원거래보다 이른 취소일자는 거절하고 원전표 상태, 전표 수, 다음 번호를 바꾸지 않는다")
    void rejectsEarlierDateWithoutChange() {
        writeThree();

        assertThrows(IllegalArgumentException.class, () -> vouchers.cancel(2, DATE.minusDays(1)));

        assertEquals(VoucherStatus.NORMAL, vouchers.findByNumber(2).status());
        assertEquals(3, vouchers.findAll().size());
        assertEquals(4, writeValid().number());
    }

    private static final Account DEPOSIT = new Account(new AccountCode("102"), "보통예금", AccountType.ASSET);
    private static final Account SALES = new Account(new AccountCode("401"), "매출", AccountType.REVENUE);
    private static final Account SUPPLIES = new Account(new AccountCode("501"), "소모품비", AccountType.EXPENSE);

    private void write(Account debit, Account credit, long amount) {
        vouchers.write(DATE, "거래", debit, credit, new Amount(amount));
    }

    @Test
    @DisplayName("거래가 없는 계정의 잔액은 0원이다")
    void balanceIsZeroWithoutEntries() {
        write(CASH, LOAN, 100_000L);

        assertEquals(0L, vouchers.balanceOf(DEPOSIT));
    }

    @Test
    @DisplayName("자산은 차변 누계 - 대변 누계, 부채는 대변 누계 - 차변 누계로 집계한다")
    void sumsByAccountType() {
        write(CASH, LOAN, 100_000L);    // 차입: 현금 +100,000 / 차입금 +100,000
        write(LOAN, CASH, 30_000L);     // 상환: 현금 -30,000 / 차입금 -30,000

        assertEquals(70_000L, vouchers.balanceOf(CASH));
        assertEquals(70_000L, vouchers.balanceOf(LOAN));
    }

    @Test
    @DisplayName("수익·비용 전표의 자산 쪽 행도 자산 잔액에 반영한다")
    void includesAssetSideOfRevenueAndExpense() {
        write(CASH, SALES, 50_000L);      // 매출: 현금 +50,000
        write(SUPPLIES, CASH, 10_000L);   // 소모품 구입: 현금 -10,000

        assertEquals(40_000L, vouchers.balanceOf(CASH));
        assertEquals(50_000L, vouchers.balanceOf(SALES));
        assertEquals(10_000L, vouchers.balanceOf(SUPPLIES));
    }

    @Test
    @DisplayName("같은 분류끼리의 거래는 한쪽을 늘리고 다른 쪽을 줄인다")
    void movesBetweenAssets() {
        write(CASH, LOAN, 100_000L);
        write(DEPOSIT, CASH, 40_000L);    // 현금을 예금에 입금

        assertEquals(60_000L, vouchers.balanceOf(CASH));
        assertEquals(40_000L, vouchers.balanceOf(DEPOSIT));
    }

    @Test
    @DisplayName("감소 쪽 누계가 더 크면 음수 잔액을 그대로 돌려준다")
    void keepsNegativeBalance() {
        write(SUPPLIES, CASH, 10_000L);

        assertEquals(-10_000L, vouchers.balanceOf(CASH));
    }

    @Test
    @DisplayName("취소된 원전표와 취소 전표를 함께 집계해 금액이 상쇄된다")
    void cancelOffsetsOriginal() {
        write(CASH, LOAN, 100_000L);
        write(SUPPLIES, CASH, 10_000L);
        vouchers.cancel(2, NEXT_DAY);

        assertEquals(100_000L, vouchers.balanceOf(CASH));
        assertEquals(0L, vouchers.balanceOf(SUPPLIES));
        assertEquals(100_000L, vouchers.balanceOf(LOAN));
    }

    @Test
    @DisplayName("상한 금액 전표 3건의 누계 3,000,000,000원을 정확하게 계산한다")
    void sumsBeyondIntRange() {
        write(CASH, LOAN, 1_000_000_000L);
        write(CASH, LOAN, 1_000_000_000L);
        write(CASH, LOAN, 1_000_000_000L);

        assertEquals(3_000_000_000L, vouchers.balanceOf(CASH));
        assertEquals(3_000_000_000L, vouchers.balanceOf(LOAN));
    }
}
