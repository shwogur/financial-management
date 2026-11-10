package org.example.view.input;

import org.example.domain.account.AccountCode;
import org.example.domain.voucher.Amount;

import java.time.LocalDate;

public record VoucherInput(
        LocalDate date,
        String description,
        AccountCode debitCode,
        AccountCode creditCode,
        Amount amount) {
}
