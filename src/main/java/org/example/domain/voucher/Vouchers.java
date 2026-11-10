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

    private int nextNumber() {
        // 전표는 삭제되지 않으므로(2장) 지금까지 저장된 전표 수 + 1이 항상 다음 번호다.
        // 실패한 요청은 저장되지 않으므로 번호도 소비하지 않는다(4.3).
        return writtenVouchers.size() + 1;
    }
}
