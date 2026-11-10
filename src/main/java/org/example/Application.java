package org.example;

import org.example.controller.MenuController;
import org.example.domain.Accounts;
import org.example.domain.Vouchers;
import org.example.view.InputView;
import org.example.view.OutputView;

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