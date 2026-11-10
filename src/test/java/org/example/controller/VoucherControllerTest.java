package org.example.controller;

import org.example.domain.Account;
import org.example.domain.AccountCode;
import org.example.domain.AccountType;
import org.example.domain.Accounts;
import org.example.domain.Amount;
import org.example.domain.Voucher;
import org.example.domain.Vouchers;
import org.example.view.InputEndedException;
import org.example.view.InputView;
import org.example.view.OutputView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VoucherControllerTest {

    private static final String PROMPT = "전표를 입력해 주세요. (거래일자,적요,차변코드,대변코드,금액)\n";
    private static final String NOT_ENOUGH_ACCOUNTS = "[ERROR] 전표 작성에는 계정과목이 2개 이상 필요합니다.\n";

    private ByteArrayOutputStream buffer;
    private Accounts accounts;
    private Vouchers vouchers;
    private InputView inputView;

    @BeforeEach
    void setUp() {
        buffer = new ByteArrayOutputStream();
        accounts = new Accounts();
        vouchers = new Vouchers();
    }

    private VoucherController controllerWithInput(String input) {
        inputView = new InputView(new BufferedReader(new StringReader(input)));
        OutputView outputView = new OutputView(new PrintStream(buffer, true, StandardCharsets.UTF_8));
        return new VoucherController(inputView, outputView, new RetryHandler(outputView), accounts, vouchers);
    }

    private String output() {
        return buffer.toString(StandardCharsets.UTF_8).replace("\r\n", "\n");
    }

    private void registerCashAndLoan() {
        accounts.register(new Account(new AccountCode("101"), "현금", AccountType.ASSET));
        accounts.register(new Account(new AccountCode("201"), "차입금", AccountType.LIABILITY));
    }

    @Test
    @DisplayName("계정이 없으면 입력 안내 없이 오류만 출력하고 입력을 읽지 않는다")
    void rejectsWithoutAccounts() {
        controllerWithInput("다음 메뉴 입력\n").write();

        assertEquals(NOT_ENOUGH_ACCOUNTS, output());
        assertTrue(vouchers.findAll().isEmpty());
        assertEquals("다음 메뉴 입력", inputView.readLine());
    }

    @Test
    @DisplayName("계정이 1개뿐이면 입력 안내 없이 오류만 출력한다")
    void rejectsWithOneAccount() {
        accounts.register(new Account(new AccountCode("101"), "현금", AccountType.ASSET));

        controllerWithInput("").write();

        assertEquals(NOT_ENOUGH_ACCOUNTS, output());
    }

    @Test
    @DisplayName("전표를 입력받아 작성하고 전표번호를 출력한다")
    void writesVoucher() {
        registerCashAndLoan();

        controllerWithInput("2026-11-06,운영자금 차입,101,201,100000\n").write();

        assertEquals(PROMPT + "전표가 작성되었습니다. (전표번호: 1)\n", output());

        List<Voucher> written = vouchers.findAll();
        assertEquals(1, written.size());
        Voucher voucher = written.get(0);
        assertEquals(LocalDate.of(2026, 11, 6), voucher.date());
        assertEquals("현금", voucher.debit().name());
        assertEquals("차입금", voucher.credit().name());
        assertEquals(100_000L, voucher.amount().value());
    }

    @Test
    @DisplayName("미등록 계정이면 같은 안내로 다시 입력받고, 실패한 요청은 번호를 소비하지 않는다")
    void retriesOnUnregisteredAccount() {
        registerCashAndLoan();

        controllerWithInput("""
                2026-11-06,운영자금 차입,101,999,100000
                2026-11-06,운영자금 차입,101,201,100000
                """).write();

        assertEquals(PROMPT
                + "[ERROR] 등록되지 않은 계정 코드입니다. (999)\n"
                + PROMPT
                + "전표가 작성되었습니다. (전표번호: 1)\n", output());
    }

    @Test
    @DisplayName("같은 계정 코드나 잘못된 금액을 거듭 입력해도 성공할 때까지 같은 안내로 다시 입력받는다")
    void retriesUntilValidInput() {
        registerCashAndLoan();
        vouchers.write(LocalDate.of(2026, 11, 5), "기존 전표",
                accounts.findByCode(new AccountCode("101")),
                accounts.findByCode(new AccountCode("201")),
                new Amount(1_000L));

        controllerWithInput("""
                2026-11-06,같은 계정,101,101,10000
                2026-11-06,사무용품 구입,201,101,-10000
                2026-11-06,사무용품 구입,201,101,10000
                """).write();

        String output = output();
        assertEquals(3, output.split("전표를 입력해 주세요", -1).length - 1);
        assertEquals(2, output.split("\\[ERROR]", -1).length - 1);
        assertTrue(output.endsWith("전표가 작성되었습니다. (전표번호: 2)\n"));
        assertEquals(2, vouchers.findAll().size());
    }

    @Test
    @DisplayName("전표 입력 중 입력이 끝나면 저장하지 않고 입력 종료를 전달한다")
    void doesNotSaveWhenInputEnds() {
        registerCashAndLoan();
        VoucherController controller = controllerWithInput("");

        assertThrows(InputEndedException.class, controller::write);
        assertTrue(vouchers.findAll().isEmpty());
    }

    @Test
    @DisplayName("작성된 전표 목록을 입력 없이 출력한다")
    void showsAllVouchers() {
        registerCashAndLoan();
        vouchers.write(LocalDate.of(2026, 11, 6), "운영자금 차입",
                accounts.findByCode(new AccountCode("101")),
                accounts.findByCode(new AccountCode("201")),
                new Amount(100_000L));

        controllerWithInput("").showAll();

        assertEquals("""
                [전표 목록]
                번호 | 거래일자 | 구분 | 상태 | 원전표 | 금액 | 적요
                1 | 2026-11-06 | 일반 | 정상 | - | 100,000원 | 운영자금 차입
                """, output());
    }

    private void writeLoanVoucher() {
        vouchers.write(LocalDate.of(2026, 11, 6), "운영자금 차입",
                accounts.findByCode(new AccountCode("101")),
                accounts.findByCode(new AccountCode("201")),
                new Amount(100_000L));
    }

    @Test
    @DisplayName("전표가 없으면 상세 조회 안내 없이 오류만 출력하고 입력을 읽지 않는다")
    void rejectsDetailWithoutVouchers() {
        controllerWithInput("다음 메뉴 입력\n").showDetail();

        assertEquals("[ERROR] 등록된 전표가 없습니다.\n", output());
        assertEquals("다음 메뉴 입력", inputView.readLine());
    }

    @Test
    @DisplayName("전표번호를 입력받아 상세를 출력한다")
    void showsDetail() {
        registerCashAndLoan();
        writeLoanVoucher();

        controllerWithInput("1\n").showDetail();

        String output = output();
        assertTrue(output.startsWith("조회할 전표번호를 입력해 주세요.\n[전표 상세]\n전표번호: 1\n"));
        assertTrue(output.endsWith("차변: 101 현금 | 100,000원\n대변: 201 차입금 | 100,000원\n"));
    }

    @Test
    @DisplayName("없는 번호나 잘못된 형식이면 같은 안내로 다시 입력받는다")
    void retriesDetailOnUnknownOrInvalidNumber() {
        registerCashAndLoan();
        writeLoanVoucher();

        controllerWithInput("2\n01\n1\n").showDetail();

        String output = output();
        assertTrue(output.startsWith("조회할 전표번호를 입력해 주세요.\n"
                + "[ERROR] 존재하지 않는 전표번호입니다. (2)\n"
                + "조회할 전표번호를 입력해 주세요.\n"
                + "[ERROR] 전표번호는 1부터 2,147,483,647까지의 숫자만 입력해야 합니다.\n"
                + "조회할 전표번호를 입력해 주세요.\n"
                + "[전표 상세]\n"));
    }
}
