package org.example.domain.voucher;

import org.example.domain.account.Account;

import java.util.Objects;

public record AccountBalance(Account account, long balance) {

    public AccountBalance {
        Objects.requireNonNull(account);
    }
}
