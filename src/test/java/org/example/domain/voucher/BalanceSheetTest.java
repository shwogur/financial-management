package org.example.domain.voucher;

import org.example.domain.account.Account;
import org.example.domain.account.AccountCode;
import org.example.domain.account.AccountType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BalanceSheetTest {

    private static final LocalDate DATE = LocalDate.of(2026, 11, 6);
    private static final Account CASH = account("101", "현금", AccountType.ASSET);
    private static final Account DEPOSIT = account("102", "보통예금", AccountType.ASSET);
    private static final Account LOAN = account("201", "차입금", AccountType.LIABILITY);
    private static final Account SERVICE_REVENUE = account("401", "용역수익", AccountType.REVENUE);
    private static final Account SUPPLIES = account("501", "소모품비", AccountType.EXPENSE);
    private static final List<Account> ALL_ACCOUNTS = List.of(CASH, DEPOSIT, LOAN, SERVICE_REVENUE, SUPPLIES);

    private Vouchers vouchers;

    private static Account account(String code, String name, AccountType type) {
        return new Account(new AccountCode(code), name, type);
    }

    @BeforeEach
    void setUp() {
        vouchers = new Vouchers();
    }

    private void write(String description, Account debit, Account credit, long amount) {
        vouchers.write(DATE, description, debit, credit, new Amount(amount));
    }

    @Test
    @DisplayName("자산과 부채 계정만 분류별로 담고 수익·비용 계정은 제외한다")
    void containsOnlyAssetsAndLiabilities() {
        BalanceSheet sheet = BalanceSheet.of(ALL_ACCOUNTS, vouchers);

        assertAll(
                () -> assertEquals(List.of(CASH, DEPOSIT), accountsOf(sheet.assets())),
                () -> assertEquals(List.of(LOAN), accountsOf(sheet.liabilities()))
        );
    }

    @Test
    @DisplayName("거래가 없는 계정도 0원으로 포함한다")
    void includesZeroBalanceAccounts() {
        write("운영자금 차입", CASH, LOAN, 100_000L);

        BalanceSheet sheet = BalanceSheet.of(ALL_ACCOUNTS, vouchers);

        assertEquals(new AccountBalance(DEPOSIT, 0L), sheet.assets().get(1));
    }

    @Test
    @DisplayName("6장 시나리오 9: 차입, 소모품 구입, 용역대금 수령 후 자산 120,000원, 부채 100,000원")
    void matchesChapterSixBeforeCancel() {
        write("운영자금 차입", CASH, LOAN, 100_000L);
        write("사무용품 구입", SUPPLIES, CASH, 10_000L);
        write("용역대금 수령", CASH, SERVICE_REVENUE, 30_000L);

        BalanceSheet sheet = BalanceSheet.of(ALL_ACCOUNTS, vouchers);

        assertAll(
                () -> assertEquals(new AccountBalance(CASH, 120_000L), sheet.assets().get(0)),
                () -> assertEquals(120_000L, sheet.assetTotal()),
                () -> assertEquals(new AccountBalance(LOAN, 100_000L), sheet.liabilities().get(0)),
                () -> assertEquals(100_000L, sheet.liabilityTotal())
        );
    }

    @Test
    @DisplayName("6장 시나리오 13: 소모품 구입을 취소하면 자산 130,000원, 부채 100,000원")
    void matchesChapterSixAfterCancel() {
        write("운영자금 차입", CASH, LOAN, 100_000L);
        write("사무용품 구입", SUPPLIES, CASH, 10_000L);
        write("용역대금 수령", CASH, SERVICE_REVENUE, 30_000L);
        vouchers.cancel(2, DATE.plusDays(1));

        BalanceSheet sheet = BalanceSheet.of(ALL_ACCOUNTS, vouchers);

        assertAll(
                () -> assertEquals(130_000L, sheet.assetTotal()),
                () -> assertEquals(100_000L, sheet.liabilityTotal())
        );
    }

    @Test
    @DisplayName("합계는 음수 잔액을 부호 그대로 더한다")
    void sumsNegativeBalancesWithSign() {
        write("예금 입금", DEPOSIT, CASH, 30_000L);    // 현금 -30,000 / 예금 +30,000
        write("소모품 구입", SUPPLIES, CASH, 5_000L);   // 현금 -5,000

        BalanceSheet sheet = BalanceSheet.of(ALL_ACCOUNTS, vouchers);

        assertAll(
                () -> assertEquals(new AccountBalance(CASH, -35_000L), sheet.assets().get(0)),
                () -> assertEquals(-5_000L, sheet.assetTotal())
        );
    }

    @Test
    @DisplayName("입력 순서와 관계없이 각 분류 안에서 코드 오름차순으로 담는다")
    void sortsByCodeWithinType() {
        BalanceSheet sheet = BalanceSheet.of(List.of(SUPPLIES, LOAN, DEPOSIT, CASH), vouchers);

        assertEquals(List.of(CASH, DEPOSIT), accountsOf(sheet.assets()));
    }

    @Test
    @DisplayName("해당 분류의 계정이 없으면 빈 목록과 0원 합계를 가진다")
    void emptyWhenNoAccountsOfType() {
        BalanceSheet sheet = BalanceSheet.of(List.of(SERVICE_REVENUE, SUPPLIES), vouchers);

        assertAll(
                () -> assertTrue(sheet.assets().isEmpty()),
                () -> assertEquals(0L, sheet.assetTotal()),
                () -> assertTrue(sheet.liabilities().isEmpty()),
                () -> assertEquals(0L, sheet.liabilityTotal())
        );
    }

    @Test
    @DisplayName("현황 목록은 바깥에서 수정할 수 없다")
    void listsAreUnmodifiable() {
        BalanceSheet sheet = BalanceSheet.of(ALL_ACCOUNTS, vouchers);

        assertThrows(UnsupportedOperationException.class, () -> sheet.assets().clear());
    }

    private static List<Account> accountsOf(List<AccountBalance> balances) {
        return balances.stream()
                .map(AccountBalance::account)
                .toList();
    }
}
