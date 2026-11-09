package org.example.domain;

import java.time.LocalDate;
import java.util.Objects;

public class Voucher {

    private static final int MIN_DESCRIPTION_LENGTH = 1;
    private static final int MAX_DESCRIPTION_LENGTH = 50;

    private final int number;
    private final LocalDate date;
    private final String description;
    private final Account debit;
    private final Account credit;
    private final Amount amount;
    private final VoucherType type;
    private final VoucherStatus status;

    private Voucher(int number, LocalDate date, String description, Account debit, Account credit,
                    Amount amount, VoucherType type, VoucherStatus status) {
        validateDifferentAccounts(debit, credit);
        this.number = number;
        this.date = Objects.requireNonNull(date);
        this.description = validateDescription(description);
        this.debit = debit;
        this.credit = credit;
        this.amount = Objects.requireNonNull(amount);
        this.type = type;
        this.status = status;
    }

    static Voucher normal(int number, LocalDate date, String description,
                          Account debit, Account credit, Amount amount) {
        return new Voucher(number, date, description, debit, credit, amount,
                VoucherType.NORMAL, VoucherStatus.NORMAL);
    }

    private static void validateDifferentAccounts(Account debit, Account credit) {
        AccountCode debitCode = Objects.requireNonNull(debit).code();
        AccountCode creditCode = Objects.requireNonNull(credit).code();
        if (debitCode.equals(creditCode)) {
            throw new IllegalArgumentException("차변과 대변에 같은 계정 코드를 선택할 수 없습니다.");
        }
    }

    private static String validateDescription(String description) {
        String stripped = Objects.requireNonNull(description).strip();
        if (stripped.length() < MIN_DESCRIPTION_LENGTH || stripped.length() > MAX_DESCRIPTION_LENGTH) {
            throw new IllegalArgumentException("적요는 1자 이상 50자 이하여야 합니다.");
        }
        return stripped;
    }

    public int number() {
        return number;
    }

    public LocalDate date() {
        return date;
    }

    public String description() {
        return description;
    }

    public Account debit() {
        return debit;
    }

    public Account credit() {
        return credit;
    }

    public Amount amount() {
        return amount;
    }

    public VoucherType type() {
        return type;
    }

    public VoucherStatus status() {
        return status;
    }
}
