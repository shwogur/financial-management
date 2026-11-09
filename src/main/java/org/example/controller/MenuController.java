package org.example.controller;

import org.example.domain.Accounts;
import org.example.view.InputEndedException;
import org.example.view.InputView;
import org.example.view.OutputView;

public class MenuController {

    private final InputView inputView;
    private final OutputView outputView;
    private final RetryHandler retryHandler;
    private final AccountController accountController;

    public MenuController(InputView inputView, OutputView outputView, Accounts accounts) {
        this.inputView = inputView;
        this.outputView = outputView;
        this.retryHandler = new RetryHandler(outputView);
        this.accountController = new AccountController(inputView, outputView, retryHandler, accounts);
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
        Menu menu = retryHandler.retry(this::readMenu);
        while (menu != Menu.EXIT) {
            execute(menu);
            menu = retryHandler.retry(this::readMenu);
        }
    }

    private Menu readMenu() {
        outputView.printMenu();
        return Menu.from(inputView.readLine());
    }

    private void execute(Menu menu) {
        switch (menu) {
            case REGISTER_ACCOUNT -> accountController.register();
            case SHOW_ACCOUNTS -> accountController.show();
            default -> outputView.printError("아직 지원하지 않는 메뉴입니다.");
        }
    }
}