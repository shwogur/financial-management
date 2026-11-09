package org.example.controller;

import org.example.domain.Account;
import org.example.domain.AccountCode;
import org.example.domain.Accounts;
import org.example.view.InputView;
import org.example.view.OutputView;

class AccountController {

    private final InputView inputView;
    private final OutputView outputView;
    private final RetryHandler retryHandler;
    private final Accounts accounts;

    AccountController(InputView inputView, OutputView outputView,
                      RetryHandler retryHandler, Accounts accounts) {
        this.inputView = inputView;
        this.outputView = outputView;
        this.retryHandler = retryHandler;
        this.accounts = accounts;
    }

    void register() {
        AccountCode code = retryHandler.retry(this::readAndRegister);
        outputView.printAccountRegistered(code);
    }

    void show() {
        outputView.printAccounts(accounts.findAll());
    }

    private AccountCode readAndRegister() {
        outputView.printAccountRegisterPrompt();
        Account account = inputView.readAccount();
        accounts.register(account);
        return account.code();
    }
}