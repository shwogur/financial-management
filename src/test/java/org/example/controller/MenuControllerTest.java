package org.example.controller;

import org.example.domain.Accounts;
import org.example.domain.Vouchers;
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
    private static final String VOUCHER_PROMPT = "전표를 입력해 주세요. (거래일자,적요,차변코드,대변코드,금액)\n";
    private static final String EXIT = "프로그램을 종료합니다.\n";
    private static final String FOUR_ACCOUNTS = """
            1
            101,현금,자산
            1
            201,차입금,부채
            1
            401,용역수익,수익
            1
            501,소모품비,비용
            """;

    private static String run(String input, Accounts accounts, Vouchers vouchers) {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        InputView inputView = new InputView(new BufferedReader(new StringReader(input)));
        OutputView outputView = new OutputView(new PrintStream(buffer, true, StandardCharsets.UTF_8));

        new MenuController(inputView, outputView, accounts, vouchers).run();

        return buffer.toString(StandardCharsets.UTF_8).replace("\r\n", "\n");
    }

    private static String run(String input, Accounts accounts) {
        return run(input, accounts, new Vouchers());
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

    @Test
    @DisplayName("계정과목이 2개 미만이면 전표 입력 안내 없이 오류를 출력하고 메뉴로 돌아간다")
    void returnsToMenuWhenNotEnoughAccounts() {
        String expected = MENU
                + "[ERROR] 전표 작성에는 계정과목이 2개 이상 필요합니다.\n"
                + MENU
                + EXIT;

        assertEquals(expected, run("3\n0\n"));
    }

    @Test
    @DisplayName("계정 등록 후 전표 3건을 작성하면 1번부터 순서대로 번호가 붙는다 (6장 1~8번)")
    void writesVouchersInScenarioOrder() {
        String input = FOUR_ACCOUNTS + """
                3
                2026-11-06,운영자금 차입,101,201,100000
                3
                2026-11-06,사무용품 구입,501,101,10000
                3
                2026-11-06,용역대금 수령,101,401,30000
                0
                """;

        String output = run(input);

        assertTrue(output.contains(VOUCHER_PROMPT + "전표가 작성되었습니다. (전표번호: 1)\n"));
        assertTrue(output.contains(VOUCHER_PROMPT + "전표가 작성되었습니다. (전표번호: 2)\n"));
        assertTrue(output.contains(VOUCHER_PROMPT + "전표가 작성되었습니다. (전표번호: 3)\n"));
        assertTrue(output.endsWith(EXIT));
    }

    @Test
    @DisplayName("잘못된 전표 입력 후 같은 안내로 돌아오고 수정 입력이 성공한다 (5.11 예시)")
    void retriesVoucherInputWithSamePrompt() {
        String input = FOUR_ACCOUNTS + """
                3
                2026-11-06,사무용품 구입,501,101,-10000
                2026-11-06,사무용품 구입,501,101,10000
                0
                """;

        String output = run(input);

        assertTrue(output.contains(VOUCHER_PROMPT
                + "[ERROR] 금액은 부호, 소수점, 쉼표, 불필요한 앞자리 0 없이 숫자만 입력해야 합니다.\n"
                + VOUCHER_PROMPT
                + "전표가 작성되었습니다. (전표번호: 1)\n"
                + MENU));
    }

    @Test
    @DisplayName("전표 입력 도중 입력이 끝나면 전표를 저장하지 않고 종료한다")
    void doesNotSaveWhenInputEndsDuringVoucherInput() {
        Vouchers vouchers = new Vouchers();

        String output = run(FOUR_ACCOUNTS + "3\n", new Accounts(), vouchers);

        assertTrue(vouchers.findAll().isEmpty());
        assertTrue(output.endsWith(VOUCHER_PROMPT + EXIT));
    }
}