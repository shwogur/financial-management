package org.example.controller;

import org.example.domain.Accounts;
import org.example.view.InputView;
import org.example.view.OutputView;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MenuControllerTest {

    private static final String MENU = """
            [회계 전표 관리]
            1. 계정과목 등록
            2. 계정과목 조회
            3. 전표 작성
            4. 전표 취소
            5. 전표 목록 조회
            6. 전표 상세 조회
            7. 자산·부채 현황 조회
            0. 종료
            메뉴를 선택해 주세요.
            """;
    private static final String ACCOUNT_PROMPT = "계정과목을 입력해 주세요. (코드,계정명,분류)\n";
    private static final String EXIT = "프로그램을 종료합니다.\n";

    private static String run(String input, Accounts accounts) {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        InputView inputView = new InputView(new BufferedReader(new StringReader(input)));
        OutputView outputView = new OutputView(new PrintStream(buffer, true, StandardCharsets.UTF_8));

        new MenuController(inputView, outputView, accounts).run();

        return buffer.toString(StandardCharsets.UTF_8).replace("\r\n", "\n");
    }

    private static String run(String input) {
        return run(input, new Accounts());
    }

    @Test
    @DisplayName("0을 입력하면 종료 문구를 출력하고 종료한다")
    void exitsWithZero() {
        assertEquals(MENU + EXIT, run("0\n"));
    }

    @Test
    @DisplayName("입력 스트림이 끝나면 종료 문구를 출력하고 종료한다")
    void exitsWhenInputEnds() {
        assertEquals(MENU + EXIT, run(""));
    }

    @Test
    @DisplayName("처음 실행한 상태에서 계정 목록은 비어 있다")
    void showsEmptyAccountsAtStart() {
        String expected = MENU
                + "[계정과목 목록]\n"
                + "등록된 계정과목이 없습니다.\n"
                + MENU
                + EXIT;

        assertEquals(expected, run("2\n0\n"));
    }

    @Test
    @DisplayName("잘못된 메뉴를 입력하면 오류를 출력하고 메뉴 전체를 다시 출력한다")
    void reprintsMenuAfterInvalidMenu() {
        String expected = MENU
                + "[ERROR] 메뉴는 0부터 7까지의 숫자 중 하나여야 합니다.\n"
                + MENU
                + EXIT;

        assertEquals(expected, run("9\n0\n"));
    }

    @Test
    @DisplayName("잘못된 계정 입력 후 같은 안내로 돌아오고 수정 입력이 성공한다")
    void retriesAccountInputWithSamePrompt() {
        String expected = MENU
                + ACCOUNT_PROMPT
                + "[ERROR] 계정 분류는 자산, 부채, 수익, 비용 중 하나여야 합니다.\n"
                + ACCOUNT_PROMPT
                + "계정과목이 등록되었습니다. (101)\n"
                + MENU
                + EXIT;

        assertEquals(expected, run("1\n101,현금,자본\n101,현금,자산\n0\n"));
    }

    @Test
    @DisplayName("네 가지 분류의 계정을 등록하고 코드 오름차순으로 조회한다")
    void registersAndShowsAccounts() {
        String input = """
                1
                101,현금,자산
                1
                201,차입금,부채
                1
                401,용역수익,수익
                1
                501,소모품비,비용
                2
                0
                """;

        String output = run(input);

        assertTrue(output.contains("""
                [계정과목 목록]
                코드 | 계정명 | 분류
                101 | 현금 | 자산
                201 | 차입금 | 부채
                401 | 용역수익 | 수익
                501 | 소모품비 | 비용
                """));
        assertTrue(output.endsWith(EXIT));
    }

    @Test
    @DisplayName("계정 입력 도중 입력이 끝나면 저장하지 않고 종료한다")
    void doesNotSaveWhenInputEndsDuringAccountInput() {
        Accounts accounts = new Accounts();

        String output = run("1\n", accounts);

        assertTrue(accounts.findAll().isEmpty());
        assertEquals(MENU + ACCOUNT_PROMPT + EXIT, output);
    }
}