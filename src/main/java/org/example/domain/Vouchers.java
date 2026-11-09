package org.example.domain;

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

    public List<Voucher> findAll() {
        return List.copyOf(writtenVouchers);
    }

    private int nextNumber() {
        // 전표는 삭제되지 않으므로(2장) 지금까지 저장된 전표 수 + 1이 항상 다음 번호다.
        // 실패한 요청은 저장되지 않으므로 번호도 소비하지 않는다(4.3).
        return writtenVouchers.size() + 1;
    }
}
