package org.example;

import org.example.controller.AccountingController;
import org.example.domain.Accounts;
import org.example.view.InputView;
import org.example.view.OutputView;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class Application {

    public static void main(String[] args) {
        InputView inputView = new InputView(new BufferedReader(new InputStreamReader(System.in)));
        OutputView outputView = new OutputView(System.out);
        AccountingController controller = new AccountingController(inputView, outputView, new Accounts());
        controller.run();
    }
}