package org.example.view.output;

import org.example.domain.account.Account;
import org.example.domain.account.AccountCode;
import org.example.domain.voucher.EntrySide;
import org.example.domain.voucher.Voucher;

import java.io.PrintStream;
import java.util.List;
import java.util.Locale;
import java.util.OptionalInt;

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
    private static final String VOUCHER_ROW_FORMAT = "%d | %s | %s | %s | %s | %s | %s";
    private static final String ENTRY_FORMAT = "%s: %s %s | %s";
    private static final String MONEY_FORMAT = "%,d원";
    private static final String NO_ORIGINAL_NUMBER = "-";

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

    public void printVoucherWritePrompt() {
        out.println("전표를 입력해 주세요. (거래일자,적요,차변코드,대변코드,금액)");
    }

    public void printVoucherWritten(int voucherNumber) {
        out.println("전표가 작성되었습니다. (전표번호: " + voucherNumber + ")");
    }

    public void printVouchers(List<Voucher> vouchers) {
        out.println("[전표 목록]");
        if (vouchers.isEmpty()) {
            out.println("등록된 전표가 없습니다.");
            return;
        }
        out.println("번호 | 거래일자 | 구분 | 상태 | 원전표 | 금액 | 적요");
        vouchers.forEach(this::printVoucherRow);
    }

    private void printVoucherRow(Voucher voucher) {
        out.println(String.format(VOUCHER_ROW_FORMAT,
                voucher.number(),
                voucher.date(),
                voucher.type().displayName(),
                voucher.status().displayName(),
                formatOriginalNumber(voucher.originalNumber()),
                formatMoney(voucher.amount().value()),
                voucher.description()));
    }

    public void printCancelPrompt() {
        out.println("취소 정보를 입력해 주세요. (원전표번호,취소일자)");
    }

    public void printVoucherCanceled(int originalNumber, int cancelNumber) {
        out.println("전표가 취소되었습니다. (원전표번호: " + originalNumber + ", 취소전표번호: " + cancelNumber + ")");
    }

    public void printVoucherNumberPrompt() {
        out.println("조회할 전표번호를 입력해 주세요.");
    }

    public void printVoucherDetail(Voucher voucher) {
        out.println("[전표 상세]");
        out.println("전표번호: " + voucher.number());
        out.println("거래일자: " + voucher.date());
        out.println("구분: " + voucher.type().displayName());
        out.println("상태: " + voucher.status().displayName());
        out.println("원전표번호: " + formatOriginalNumber(voucher.originalNumber()));
        out.println("적요: " + voucher.description());
        printEntry(EntrySide.DEBIT, voucher);
        printEntry(EntrySide.CREDIT, voucher);
    }

    private void printEntry(EntrySide side, Voucher voucher) {
        Account account = voucher.accountOn(side);
        out.println(String.format(ENTRY_FORMAT,
                side.displayName(), account.code().value(), account.name(), formatMoney(voucher.amount().value())));
    }

    public void printNoVouchers() {
        printError("등록된 전표가 없습니다.");
    }

    private String formatOriginalNumber(OptionalInt originalNumber) {
        if (originalNumber.isEmpty()) {
            return NO_ORIGINAL_NUMBER;
        }
        return String.valueOf(originalNumber.getAsInt());
    }

    private String formatMoney(long value) {
        return String.format(Locale.US, MONEY_FORMAT, value);
    }

    public void printNotEnoughAccountsForVoucher() {
        printError("전표 작성에는 계정과목이 2개 이상 필요합니다.");
    }

    public void printError(String message) {
        out.println(ERROR_PREFIX + message);
    }

    public void printExit() {
        out.println("프로그램을 종료합니다.");
    }
}