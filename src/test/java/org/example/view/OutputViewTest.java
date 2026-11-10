package org.example.view;

import org.example.domain.Account;
import org.example.domain.AccountCode;
import org.example.domain.AccountType;
import org.example.domain.Amount;
import org.example.domain.Vouchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
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

    @Test
    @DisplayName("전표 입력 안내를 지정된 문구로 출력한다")
    void printsVoucherWritePrompt() {
        outputView.printVoucherWritePrompt();

        assertEquals("전표를 입력해 주세요. (거래일자,적요,차변코드,대변코드,금액)\n", output());
    }

    @Test
    @DisplayName("전표 작성 성공 메시지에 전표번호를 출력한다")
    void printsVoucherWritten() {
        outputView.printVoucherWritten(1);

        assertEquals("전표가 작성되었습니다. (전표번호: 1)\n", output());
    }

    @Test
    @DisplayName("전표번호는 천 단위 쉼표 없이 출력한다")
    void printsVoucherNumberWithoutComma() {
        outputView.printVoucherWritten(1234);

        assertEquals("전표가 작성되었습니다. (전표번호: 1234)\n", output());
    }

    @Test
    @DisplayName("계정과목이 부족하면 지정된 오류 문구를 출력한다")
    void printsNotEnoughAccountsForVoucher() {
        outputView.printNotEnoughAccountsForVoucher();

        assertEquals("[ERROR] 전표 작성에는 계정과목이 2개 이상 필요합니다.\n", output());
    }

    @Test
    @DisplayName("전표가 없으면 열 제목 없이 안내 문구만 출력한다")
    void printsEmptyVouchers() {
        outputView.printVouchers(List.of());

        assertEquals("""
                [전표 목록]
                등록된 전표가 없습니다.
                """, output());
    }

    @Test
    @DisplayName("전표 목록을 번호순으로, 금액은 천 단위 쉼표와 원, 원전표가 없으면 -로 출력한다")
    void printsVouchers() {
        Account cash = account("101", "현금", AccountType.ASSET);
        Account loan = account("201", "차입금", AccountType.LIABILITY);
        Vouchers vouchers = new Vouchers();
        LocalDate date = LocalDate.of(2026, 11, 6);
        vouchers.write(date, "운영자금 차입", cash, loan, new Amount(100_000L));
        vouchers.write(date, "최소 금액", cash, loan, new Amount(1L));
        vouchers.write(LocalDate.of(1, 1, 1), "최대 금액", cash, loan, new Amount(1_000_000_000L));

        outputView.printVouchers(vouchers.findAll());

        assertEquals("""
                [전표 목록]
                번호 | 거래일자 | 구분 | 상태 | 원전표 | 금액 | 적요
                1 | 2026-11-06 | 일반 | 정상 | - | 100,000원 | 운영자금 차입
                2 | 2026-11-06 | 일반 | 정상 | - | 1원 | 최소 금액
                3 | 0001-01-01 | 일반 | 정상 | - | 1,000,000,000원 | 최대 금액
                """, output());
    }

    @Test
    @DisplayName("상세 조회 입력 안내를 지정된 문구로 출력한다")
    void printsVoucherNumberPrompt() {
        outputView.printVoucherNumberPrompt();

        assertEquals("조회할 전표번호를 입력해 주세요.\n", output());
    }

    @Test
    @DisplayName("전표 상세를 차변·대변 계정 코드와 계정명, 금액 형식과 함께 출력한다")
    void printsVoucherDetail() {
        Vouchers vouchers = new Vouchers();
        vouchers.write(LocalDate.of(2026, 11, 6), "운영자금 차입",
                account("101", "현금", AccountType.ASSET),
                account("201", "차입금", AccountType.LIABILITY),
                new Amount(100_000L));

        outputView.printVoucherDetail(vouchers.findByNumber(1));

        assertEquals("""
                [전표 상세]
                전표번호: 1
                거래일자: 2026-11-06
                구분: 일반
                상태: 정상
                원전표번호: -
                적요: 운영자금 차입
                차변: 101 현금 | 100,000원
                대변: 201 차입금 | 100,000원
                """, output());
    }

    @Test
    @DisplayName("전표가 없으면 지정된 오류 문구를 출력한다")
    void printsNoVouchers() {
        outputView.printNoVouchers();

        assertEquals("[ERROR] 등록된 전표가 없습니다.\n", output());
    }
}