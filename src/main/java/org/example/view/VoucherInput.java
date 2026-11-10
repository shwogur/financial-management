package org.example.view;

import org.example.domain.AccountCode;
import org.example.domain.Amount;

import java.time.LocalDate;

public record VoucherInput(
        LocalDate date,
        String description,
        AccountCode debitCode,
        AccountCode creditCode,
        Amount amount) {
}
