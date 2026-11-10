package org.example.controller;

import org.example.domain.account.Account;
import org.example.domain.account.AccountCode;
import org.example.domain.account.AccountType;
import org.example.domain.account.Accounts;
import org.example.view.input.InputEndedException;
import org.example.view.input.InputView;
import org.example.view.output.OutputView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AccountControllerTest {

    private static final String PROMPT = "계정과목을 입력해 주세요. (코드,계정명,분류)\n";

    private ByteArrayOutputStream buffer;
    private Accounts accounts;

    @BeforeEach
    void setUp() {
        buffer = new ByteArrayOutputStream();
        accounts = new Accounts();
    }

    private AccountController controllerWithInput(String input) {
        InputView inputView = new InputView(new BufferedReader(new StringReader(input)));
        OutputView outputView = new OutputView(new PrintStream(buffer, true, StandardCharsets.UTF_8));
        return new AccountController(inputView, outputView, new RetryHandler(outputView), accounts);
    }

    private String output() {
        return buffer.toString(StandardCharsets.UTF_8).replace("\r\n", "\n");
    }

    @Test
    @DisplayName("계정을 입력받아 등록하고 성공 메시지를 출력한다")
    void registersAccount() {
        controllerWithInput("101,현금,자산\n").register();

        assertEquals(1, accounts.findAll().size());
        assertEquals(PROMPT + "계정과목이 등록되었습니다. (101)\n", output());
    }

    @Test
    @DisplayName("중복 코드면 같은 안내로 다시 입력받고 기존 계정은 유지한다")
    void retriesWithSamePromptOnDuplicateCode() {
        accounts.register(new Account(new AccountCode("101"), "현금", AccountType.ASSET));

        controllerWithInput("101,보통예금,자산\n102,보통예금,자산\n").register();

        List<Account> all = accounts.findAll();
        assertEquals(2, all.size());
        assertEquals("현금", all.get(0).name());
        assertEquals(PROMPT
                + "[ERROR] 이미 등록된 계정 코드입니다. (101)\n"
                + PROMPT
                + "계정과목이 등록되었습니다. (102)\n", output());
    }

    @Test
    @DisplayName("계정 입력 중 입력이 끝나면 저장하지 않고 입력 종료를 전달한다")
    void doesNotSaveWhenInputEnds() {
        AccountController controller = controllerWithInput("");

        assertThrows(InputEndedException.class, controller::register);
        assertTrue(accounts.findAll().isEmpty());
    }

    @Test
    @DisplayName("등록된 계정 목록을 출력한다")
    void showsAccounts() {
        accounts.register(new Account(new AccountCode("101"), "현금", AccountType.ASSET));

        controllerWithInput("").show();

        assertEquals("""
                [계정과목 목록]
                코드 | 계정명 | 분류
                101 | 현금 | 자산
                """, output());
    }
}