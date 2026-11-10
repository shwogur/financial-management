package org.example;

import org.example.controller.MenuController;
import org.example.domain.account.Accounts;
import org.example.domain.voucher.Vouchers;
import org.example.view.input.InputView;
import org.example.view.output.OutputView;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class Application {

    public static void main(String[] args) {
        InputView inputView = new InputView(new BufferedReader(new InputStreamReader(System.in)));
        OutputView outputView = new OutputView(System.out);
        MenuController controller = new MenuController(inputView, outputView, new Accounts(), new Vouchers());
        controller.run();
    }
}