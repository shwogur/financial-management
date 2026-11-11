package org.example.domain.voucher;

import org.example.domain.account.Account;
import org.example.domain.account.AccountType;

import java.util.Comparator;
import java.util.List;

public record BalanceSheet(List<AccountBalance> assets, List<AccountBalance> liabilities) {

    public BalanceSheet {
        assets = List.copyOf(assets);
        liabilities = List.copyOf(liabilities);
    }

    public static BalanceSheet of(List<Account> accounts, Vouchers vouchers) {
        return new BalanceSheet(
                balancesOf(AccountType.ASSET, accounts, vouchers),
                balancesOf(AccountType.LIABILITY, accounts, vouchers));
    }

    public long assetTotal() {
        return total(assets);
    }

    public long liabilityTotal() {
        return total(liabilities);
    }

    private static List<AccountBalance> balancesOf(AccountType type, List<Account> accounts, Vouchers vouchers) {
        // 수익·비용 계정은 목록에서 빠지지만, 그 전표의 자산·부채 행은 balanceOf에서 이미 집계된다(4.6).
        return accounts.stream()
                .filter(account -> account.type() == type)
                .sorted(Comparator.comparing(Account::code))
                .map(account -> new AccountBalance(account, vouchers.balanceOf(account)))
                .toList();
    }

    private static long total(List<AccountBalance> balances) {
        // 음수 잔액도 부호 그대로 더한다(4.6).
        return balances.stream()
                .mapToLong(AccountBalance::balance)
                .sum();
    }
}
