package org.example.view;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.StringReader;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class InputViewTest {

    private static InputView inputViewOf(String input) {
        return new InputView(new BufferedReader(new StringReader(input)));
    }

    @Test
    @DisplayName("입력된 줄을 순서대로 한 줄씩 읽는다")
    void readsLinesInOrder() {
        InputView inputView = inputViewOf("1\n101,현금,자산\n");

        assertEquals("1", inputView.readLine());
        assertEquals("101,현금,자산", inputView.readLine());
    }

    @Test
    @DisplayName("읽은 줄을 가공하지 않고 그대로 반환한다")
    void returnsLineAsIs() {
        InputView inputView = inputViewOf("  101 , 현금 , 자산  \n");

        assertEquals("  101 , 현금 , 자산  ", inputView.readLine());
    }

    @Test
    @DisplayName("빈 줄은 입력 종료가 아니라 빈 문자열로 읽는다")
    void readsEmptyLineAsEmptyString() {
        InputView inputView = inputViewOf("\n");

        assertEquals("", inputView.readLine());
    }

    @Test
    @DisplayName("입력이 없으면 입력 종료 예외를 던진다")
    void throwsWhenNoInput() {
        InputView inputView = inputViewOf("");

        assertThrows(InputEndedException.class, inputView::readLine);
    }

    @Test
    @DisplayName("모든 줄을 읽은 뒤에는 입력 종료 예외를 던진다")
    void throwsAfterAllLinesRead() {
        InputView inputView = inputViewOf("0\n");
        inputView.readLine();

        assertThrows(InputEndedException.class, inputView::readLine);
    }
}