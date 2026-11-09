package org.example.view;

import org.example.domain.Account;
import org.example.domain.AccountCode;
import org.example.domain.AccountType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OutputViewTest {

    private ByteArrayOutputStream buffer;
    private OutputView outputView;

    @BeforeEach
    void setUp() {
        buffer = new ByteArrayOutputStream();
        outputView = new OutputView(new PrintStream(buffer, true, StandardCharsets.UTF_8));
    }

    private String output() {
        return buffer.toString(StandardCharsets.UTF_8).replace("\r\n", "\n");
    }

    private static Account account(String code, String name, AccountType type) {
        return new Account(new AccountCode(code), name, type);
    }

    @Test
    @DisplayName("메뉴를 지정된 형식으로 출력한다")
    void printsMenu() {
        outputView.printMenu();

        assertEquals("""
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
                """, output());
    }

    @Test
    @DisplayName("계정이 없으면 열 제목 없이 안내 문구만 출력한다")
    void printsEmptyAccounts() {
        outputView.printAccounts(List.of());

        assertEquals("""
                [계정과목 목록]
                등록된 계정과목이 없습니다.
                """, output());
    }

    @Test
    @DisplayName("계정 목록을 열 제목과 함께 구분자 형식으로 출력한다")
    void printsAccounts() {
        outputView.printAccounts(List.of(
                account("101", "현금", AccountType.ASSET),
                account("201", "차입금", AccountType.LIABILITY)));

        assertEquals("""
                [계정과목 목록]
                코드 | 계정명 | 분류
                101 | 현금 | 자산
                201 | 차입금 | 부채
                """, output());
    }

    @Test
    @DisplayName("등록 성공 메시지에 앞자리 0을 유지한 코드를 출력한다")
    void printsAccountRegistered() {
        outputView.printAccountRegistered(new AccountCode("001"));

        assertEquals("계정과목이 등록되었습니다. (001)\n", output());
    }

    @Test
    @DisplayName("오류 메시지 앞에 [ERROR]를 붙인다")
    void printsErrorWithPrefix() {
        outputView.printError("이미 등록된 계정 코드입니다. (101)");

        assertEquals("[ERROR] 이미 등록된 계정 코드입니다. (101)\n", output());
    }

    @Test
    @DisplayName("종료 문구를 출력한다")
    void printsExit() {
        outputView.printExit();

        assertEquals("프로그램을 종료합니다.\n", output());
    }
}