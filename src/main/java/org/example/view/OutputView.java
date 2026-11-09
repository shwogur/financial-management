package org.example.view;

import org.example.domain.Account;
import org.example.domain.AccountCode;

import java.io.PrintStream;
import java.util.List;

public class OutputView {

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
            메뉴를 선택해 주세요.""";
    private static final String ERROR_PREFIX = "[ERROR] ";
    private static final String ACCOUNT_ROW_FORMAT = "%s | %s | %s";

    private final PrintStream out;

    public OutputView(PrintStream out) {
        this.out = out;
    }

    public void printMenu() {
        out.println(MENU);
    }

    public void printAccountRegisterPrompt() {
        out.println("계정과목을 입력해 주세요. (코드,계정명,분류)");
    }

    public void printAccountRegistered(AccountCode code) {
        out.println("계정과목이 등록되었습니다. (" + code.value() + ")");
    }

    public void printAccounts(List<Account> accounts) {
        out.println("[계정과목 목록]");
        if (accounts.isEmpty()) {
            out.println("등록된 계정과목이 없습니다.");
            return;
        }
        out.println("코드 | 계정명 | 분류");
        accounts.forEach(this::printAccountRow);
    }

    private void printAccountRow(Account account) {
        out.println(String.format(ACCOUNT_ROW_FORMAT,
                account.code().value(), account.name(), account.type().displayName()));
    }

    public void printError(String message) {
        out.println(ERROR_PREFIX + message);
    }

    public void printExit() {
        out.println("프로그램을 종료합니다.");
    }
}