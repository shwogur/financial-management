package org.example.domain;

public enum VoucherStatus {
    NORMAL("정상"),
    CANCELED("취소");

    private final String displayName;

    VoucherStatus(String displayName) {
        this.displayName = displayName;
    }

    public String displayName() {
        return displayName;
    }
}
