package org.example.view;

import org.example.domain.Account;
import org.example.domain.AccountCode;
import org.example.domain.AccountType;
import org.example.domain.Amount;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;

public class InputView {

    private static final int ACCOUNT_ITEM_COUNT = 3;
    private static final int ACCOUNT_CODE = 0;
    private static final int ACCOUNT_NAME = 1;
    private static final int ACCOUNT_TYPE = 2;

    private static final int VOUCHER_ITEM_COUNT = 5;
    private static final int VOUCHER_DATE = 0;
    private static final int VOUCHER_DESCRIPTION = 1;
    private static final int VOUCHER_DEBIT_CODE = 2;
    private static final int VOUCHER_CREDIT_CODE = 3;
    private static final int VOUCHER_AMOUNT = 4;

    private final BufferedReader reader;

    public InputView(BufferedReader reader) {
        this.reader = reader;
    }

    public String readLine() {
        String line = readFromReader();
        if (line == null) {
            throw new InputEndedException();
        }
        return line;
    }

    public Account readAccount() {
        List<String> items = InputParser.split(readLine(), ACCOUNT_ITEM_COUNT);
        return new Account(
                new AccountCode(items.get(ACCOUNT_CODE)),
                items.get(ACCOUNT_NAME),
                AccountType.from(items.get(ACCOUNT_TYPE)));
    }

    public VoucherInput readVoucher() {
        List<String> items = InputParser.split(readLine(), VOUCHER_ITEM_COUNT);
        return new VoucherInput(
                InputParser.parseDate(items.get(VOUCHER_DATE)),
                items.get(VOUCHER_DESCRIPTION),
                new AccountCode(items.get(VOUCHER_DEBIT_CODE)),
                new AccountCode(items.get(VOUCHER_CREDIT_CODE)),
                new Amount(InputParser.parseAmount(items.get(VOUCHER_AMOUNT))));
    }

    private String readFromReader() {
        try {
            return reader.readLine();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}