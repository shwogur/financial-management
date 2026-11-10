package org.example.controller;

import org.example.view.output.OutputView;

import java.util.function.Supplier;

class RetryHandler {

    private final OutputView outputView;

    RetryHandler(OutputView outputView) {
        this.outputView = outputView;
    }

    <T> T retry(Supplier<T> action) {
        while (true) {
            try {
                return action.get();
            } catch (IllegalArgumentException e) {
                outputView.printError(e.getMessage());
            }
        }
    }
}