package org.example.controller;

import org.example.domain.Account;
import org.example.domain.Accounts;
import org.example.domain.Voucher;
import org.example.domain.Vouchers;
import org.example.view.InputView;
import org.example.view.OutputView;
import org.example.view.VoucherInput;

class VoucherController {

    private static final int MIN_ACCOUNTS_FOR_VOUCHER = 2;

    private final InputView inputView;
    private final OutputView outputView;
    private final RetryHandler retryHandler;
    private final Accounts accounts;
    private final Vouchers vouchers;

    VoucherController(InputView inputView, OutputView outputView, RetryHandler retryHandler,
                      Accounts accounts, Vouchers vouchers) {
        this.inputView = inputView;
        this.outputView = outputView;
        this.retryHandler = retryHandler;
        this.accounts = accounts;
        this.vouchers = vouchers;
    }

    void write() {
        if (accounts.size() < MIN_ACCOUNTS_FOR_VOUCHER) {
            outputView.printNotEnoughAccountsForVoucher();
            return;
        }
        Voucher voucher = retryHandler.retry(this::readAndWrite);
        outputView.printVoucherWritten(voucher.number());
    }

    private Voucher readAndWrite() {
        outputView.printVoucherWritePrompt();
        VoucherInput input = inputView.readVoucher();
        Account debit = accounts.findByCode(input.debitCode());
        Account credit = accounts.findByCode(input.creditCode());
        return vouchers.write(input.date(), input.description(), debit, credit, input.amount());
    }
}
