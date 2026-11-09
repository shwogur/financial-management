package org.example.controller;

import org.example.domain.Account;
import org.example.domain.AccountCode;
import org.example.domain.AccountType;
import org.example.domain.Accounts;
import org.example.view.InputEndedException;
import org.example.view.InputParser;
import org.example.view.InputView;
import org.example.view.OutputView;

import java.util.List;
import java.util.function.Supplier;

public class AccountingController {

    private static final int ACCOUNT_ITEM_COUNT = 3;

    private final InputView inputView;
    private final OutputView outputView;
    private final Accounts accounts;

    public AccountingController(InputView inputView, OutputView outputView, Accounts accounts) {
        this.inputView = inputView;
        this.outputView = outputView;
        this.accounts = accounts;
    }

    public void run() {
        try {
            processMenusUntilExit();
        } catch (InputEndedException e) {
            // 입력 스트림이 끝나면 진행 중인 작업을 저장하지 않고 종료한다. (5.1)
        }
        outputView.printExit();
    }

    private void processMenusUntilExit() {
        Menu menu = retry(this::readMenu);
        while (menu != Menu.EXIT) {
            execute(menu);
            menu = retry(this::readMenu);
        }
    }

    private Menu readMenu() {
        outputView.printMenu();
        return Menu.from(inputView.readLine());
    }

    private void execute(Menu menu) {
        switch (menu) {
            case REGISTER_ACCOUNT -> registerAccount();
            case SHOW_ACCOUNTS -> outputView.printAccounts(accounts.findAll());
            default -> outputView.printError("아직 지원하지 않는 메뉴입니다.");
        }
    }

    private void registerAccount() {
        AccountCode code = retry(this::readAndRegisterAccount);
        outputView.printAccountRegistered(code);
    }

    private AccountCode readAndRegisterAccount() {
        outputView.printAccountRegisterPrompt();
        List<String> items = InputParser.split(inputView.readLine(), ACCOUNT_ITEM_COUNT);
        Account account = new Account(
                new AccountCode(items.get(0)),
                items.get(1),
                AccountType.from(items.get(2)));
        accounts.register(account);
        return account.code();
    }

    private <T> T retry(Supplier<T> action) {
        while (true) {
            try {
                return action.get();
            } catch (IllegalArgumentException e) {
                outputView.printError(e.getMessage());
            }
        }
    }
}