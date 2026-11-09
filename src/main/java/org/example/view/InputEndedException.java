package org.example.view;

public class InputEndedException extends RuntimeException {

    public InputEndedException() {
        super("입력이 종료되었습니다.");
    }
}