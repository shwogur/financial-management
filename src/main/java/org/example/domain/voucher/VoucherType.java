package org.example.domain.voucher;

public enum VoucherType {
    NORMAL("일반"),
    CANCEL("취소");

    private final String displayName;

    VoucherType(String displayName) {
        this.displayName = displayName;
    }

    public String displayName() {
        return displayName;
    }
}
