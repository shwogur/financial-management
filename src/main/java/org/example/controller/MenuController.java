package org.example.controller;

import org.example.domain.account.Accounts;
import org.example.domain.voucher.Vouchers;
import org.example.view.input.InputEndedException;
import org.example.view.input.InputView;
import org.example.view.output.OutputView;

public class MenuController {

    private final InputView inputView;
    private final OutputView outputView;
    private final RetryHandler retryHandler;
    private final AccountController accountController;
    private final VoucherController voucherController;

    public MenuController(InputView inputView, OutputView outputView, Accounts accounts, Vouchers vouchers) {
        this.inputView = inputView;
        this.outputView = outputView;
        this.retryHandler = new RetryHandler(outputView);
        this.accountController = new AccountController(inputView, outputView, retryHandler, accounts);
        this.voucherController = new VoucherController(inputView, outputView, retryHandler, accounts, vouchers);
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
            case WRITE_VOUCHER -> voucherController.write();
            case CANCEL_VOUCHER -> voucherController.cancel();
            case SHOW_VOUCHERS -> voucherController.showAll();
            case SHOW_VOUCHER_DETAIL -> voucherController.showDetail();
            default -> outputView.printError("아직 지원하지 않는 메뉴입니다.");
        }
    }
}
