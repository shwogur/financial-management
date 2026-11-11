package org.example.controller;

import org.example.domain.account.Account;
import org.example.domain.account.Accounts;
import org.example.domain.voucher.BalanceSheet;
import org.example.domain.voucher.Voucher;
import org.example.domain.voucher.Vouchers;
import org.example.view.input.CancelInput;
import org.example.view.input.InputView;
import org.example.view.input.VoucherInput;
import org.example.view.output.OutputView;

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

    void cancel() {
        if (vouchers.isEmpty()) {
            outputView.printNoVouchers();
            return;
        }
        Voucher reversal = retryHandler.retry(this::readAndCancel);
        outputView.printVoucherCanceled(reversal.originalNumber().getAsInt(), reversal.number());
    }

    void showAll() {
        outputView.printVouchers(vouchers.findAll());
    }

    void showDetail() {
        if (vouchers.isEmpty()) {
            outputView.printNoVouchers();
            return;
        }
        Voucher voucher = retryHandler.retry(this::readAndFind);
        outputView.printVoucherDetail(voucher);
    }

    void showBalance() {
        outputView.printBalanceSheet(BalanceSheet.of(accounts.findAll(), vouchers));
    }

    private Voucher readAndCancel() {
        outputView.printCancelPrompt();
        CancelInput input = inputView.readCancel();
        return vouchers.cancel(input.originalNumber(), input.cancelDate());
    }

    private Voucher readAndFind() {
        outputView.printVoucherNumberPrompt();
        return vouchers.findByNumber(inputView.readVoucherNumber());
    }

    private Voucher readAndWrite() {
        outputView.printVoucherWritePrompt();
        VoucherInput input = inputView.readVoucher();
        Account debit = accounts.findByCode(input.debitCode());
        Account credit = accounts.findByCode(input.creditCode());
        return vouchers.write(input.date(), input.description(), debit, credit, input.amount());
    }
}
