package org.example.domain.voucher;

import org.example.domain.account.Account;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Vouchers {

    private final List<Voucher> writtenVouchers = new ArrayList<>();

    public Voucher write(LocalDate date, String description, Account debit, Account credit, Amount amount) {
        Voucher voucher = Voucher.normal(nextNumber(), date, description, debit, credit, amount);
        writtenVouchers.add(voucher);
        return voucher;
    }

    public Voucher cancel(int originalNumber, LocalDate cancelDate) {
        Voucher original = findByNumber(originalNumber);
        Voucher reversal = original.cancel(nextNumber(), cancelDate);
        writtenVouchers.add(reversal);
        return reversal;
    }

    public Voucher findByNumber(int number) {
        return writtenVouchers.stream()
                .filter(voucher -> voucher.number() == number)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 전표번호입니다. (" + number + ")"));
    }

    public List<Voucher> findAll() {
        return List.copyOf(writtenVouchers);
    }

    public boolean isEmpty() {
        return writtenVouchers.isEmpty();
    }

    public long balanceOf(Account account) {
        long debitTotal = totalOn(account, EntrySide.DEBIT);
        long creditTotal = totalOn(account, EntrySide.CREDIT);
        return account.type().balanceOf(debitTotal, creditTotal);
    }

    private long totalOn(Account account, EntrySide side) {
        // 구분·상태로 거르지 않는다. 취소된 원전표와 취소 전표가 함께 집계되어야 서로 상쇄된다(4.6).
        return writtenVouchers.stream()
                .mapToLong(voucher -> voucher.amountOn(account, side))
                .sum();
    }

    private int nextNumber() {
        // 전표는 삭제되지 않으므로(2장) 지금까지 저장된 전표 수 + 1이 항상 다음 번호다.
        // 실패한 요청은 저장되지 않으므로 번호도 소비하지 않는다(4.3).
        return writtenVouchers.size() + 1;
    }
}
