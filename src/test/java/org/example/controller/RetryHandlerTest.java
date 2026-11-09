package org.example.controller;

import org.example.view.InputEndedException;
import org.example.view.OutputView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RetryHandlerTest {

    private static final String INVALID = "잘못된 값";

    private ByteArrayOutputStream buffer;
    private RetryHandler retryHandler;

    @BeforeEach
    void setUp() {
        buffer = new ByteArrayOutputStream();
        OutputView outputView = new OutputView(new PrintStream(buffer, true, StandardCharsets.UTF_8));
        retryHandler = new RetryHandler(outputView);
    }

    private String output() {
        return buffer.toString(StandardCharsets.UTF_8).replace("\r\n", "\n");
    }

    @Test
    @DisplayName("처음에 성공하면 오류 없이 결과를 반환한다")
    void returnsResultWithoutError() {
        String result = retryHandler.retry(() -> "성공");

        assertEquals("성공", result);
        assertEquals("", output());
    }

    @Test
    @DisplayName("잘못된 입력이면 오류를 출력하고 성공할 때까지 다시 시도한다")
    void retriesUntilSuccess() {
        Iterator<String> answers = List.of(INVALID, INVALID, "성공").iterator();

        String result = retryHandler.retry(() -> {
            String answer = answers.next();
            if (answer.equals(INVALID)) {
                throw new IllegalArgumentException("다시 입력해 주세요.");
            }
            return answer;
        });

        assertEquals("성공", result);
        assertEquals("[ERROR] 다시 입력해 주세요.\n[ERROR] 다시 입력해 주세요.\n", output());
    }

    @Test
    @DisplayName("입력 종료 예외는 잡지 않고 그대로 전달한다")
    void doesNotCatchInputEnded() {
        assertThrows(InputEndedException.class,
                () -> retryHandler.retry(() -> {
                    throw new InputEndedException();
                }));
    }
}