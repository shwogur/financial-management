package org.example.domain.voucher;

import org.example.domain.account.Account;

import java.time.LocalDate;
import java.util.Objects;
import java.util.OptionalInt;

public class Voucher {

    private static final int MAX_DESCRIPTION_LENGTH = 50;

    private final int number;
    private final LocalDate date;
    private final String description;
    private final Account debit;
    private final Account credit;
    private final Amount amount;
    private final VoucherType type;
    private VoucherStatus status;   // 유일하게 바뀌는 값: cancel()로만 정상 → 취소
    private final OptionalInt originalNumber;

    private Voucher(int number, LocalDate date, String description, Account debit, Account credit,
                    Amount amount, VoucherType type, VoucherStatus status, OptionalInt originalNumber) {
        validateDifferentAccounts(debit, credit);
        this.number = number;
        this.date = Objects.requireNonNull(date);
        this.description = validateDescription(description);
        this.debit = debit;
        this.credit = credit;
        this.amount = Objects.requireNonNull(amount);
        this.type = type;
        this.status = status;
        this.originalNumber = originalNumber;
    }

    static Voucher normal(int number, LocalDate date, String description,
                          Account debit, Account credit, Amount amount) {
        return new Voucher(number, date, description, debit, credit, amount,
                VoucherType.NORMAL, VoucherStatus.NORMAL, OptionalInt.empty());
    }

    Voucher cancel(int cancelNumber, LocalDate cancelDate) {
        validateCancelable(Objects.requireNonNull(cancelDate));
        Voucher reversal = new Voucher(cancelNumber, cancelDate, description, credit, debit, amount,
                VoucherType.CANCEL, VoucherStatus.NORMAL, OptionalInt.of(number));
        status = VoucherStatus.CANCELED;
        return reversal;
    }

    private void validateCancelable(LocalDate cancelDate) {
        if (type == VoucherType.CANCEL) {
            throw new IllegalArgumentException("취소 전표는 취소할 수 없습니다. (" + number + ")");
        }
        if (status == VoucherStatus.CANCELED) {
            throw new IllegalArgumentException("이미 취소된 전표입니다. (" + number + ")");
        }
        if (cancelDate.isBefore(date)) {
            throw new IllegalArgumentException("취소일자는 원전표 거래일자(" + date + ")와 같거나 이후여야 합니다.");
        }
    }

    private static void validateDifferentAccounts(Account debit, Account credit) {
        if (Objects.requireNonNull(debit).equals(Objects.requireNonNull(credit))) {
            throw new IllegalArgumentException("차변과 대변에 같은 계정 코드를 선택할 수 없습니다.");
        }
    }

    private static String validateDescription(String description) {
        String stripped = Objects.requireNonNull(description).strip();
        if (stripped.isEmpty() || stripped.length() > MAX_DESCRIPTION_LENGTH) {
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

    public Account accountOn(EntrySide side) {
        return switch (side) {
            case DEBIT -> debit;
            case CREDIT -> credit;
        };
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

    public OptionalInt originalNumber() {
        return originalNumber;
    }
}
