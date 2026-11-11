package org.example.domain.voucher;

public enum EntrySide {
    DEBIT("차변"),
    CREDIT("대변");

    private final String displayName;

    EntrySide(String displayName) {
        this.displayName = displayName;
    }

    public String displayName() {
        return displayName;
    }
}
