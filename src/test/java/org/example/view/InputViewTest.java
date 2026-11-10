package org.example.view;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.BufferedReader;
import java.io.StringReader;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.example.domain.Account;
import org.example.domain.AccountCode;
import org.example.domain.AccountType;
import org.example.domain.Amount;

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

    @Test
    @DisplayName("한 줄을 읽어 항목 이름에 맞게 계정으로 변환한다")
    void readsAccount() {
        InputView inputView = inputViewOf(" 001 , 보통 예금 , 자산 \n");

        Account account = inputView.readAccount();

        assertEquals(new AccountCode("001"), account.code());
        assertEquals("보통 예금", account.name());
        assertEquals(AccountType.ASSET, account.type());
    }

    @Test
    @DisplayName("계정 입력의 항목 개수가 맞지 않으면 거절한다")
    void rejectsAccountWithWrongItemCount() {
        InputView inputView = inputViewOf("101,현금\n");

        assertThrows(IllegalArgumentException.class, inputView::readAccount);
    }

    @Test
    @DisplayName("계정 입력의 값이 업무 규칙에 맞지 않으면 거절한다")
    void rejectsAccountWithInvalidValue() {
        InputView inputView = inputViewOf("101,현금,자본\n");

        assertThrows(IllegalArgumentException.class, inputView::readAccount);
    }

    @Test
    @DisplayName("계정 입력 중 입력이 끝나면 입력 종료 예외를 던진다")
    void throwsWhenInputEndsWhileReadingAccount() {
        InputView inputView = inputViewOf("");

        assertThrows(InputEndedException.class, inputView::readAccount);
    }

    @Test
    @DisplayName("한 줄을 읽어 항목 이름에 맞게 전표 입력으로 변환한다")
    void readsVoucher() {
        InputView inputView = inputViewOf(" 2026-11-06 , 운영자금 차입 , 101 , 201 , 100000 \n");

        VoucherInput input = inputView.readVoucher();

        assertEquals(new VoucherInput(
                LocalDate.of(2026, 11, 6),
                "운영자금 차입",
                new AccountCode("101"),
                new AccountCode("201"),
                new Amount(100_000L)), input);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "2026-11-06,운영자금 차입,101,201",
            "2026-11-06,운영자금 차입,101,201,100000,",
            "2026-02-30,운영자금 차입,101,201,100000",
            "2026-11-06,운영자금 차입,1,201,100000",
            "2026-11-06,운영자금 차입,101,201,-10000",
            "2026-11-06,운영자금 차입,101,201,0",
            "2026-11-06,운영자금 차입,101,201,1000000001"
    })
    @DisplayName("전표 입력의 개수, 날짜, 코드 형식, 금액이 잘못되면 거절한다")
    void rejectsInvalidVoucherInput(String line) {
        InputView inputView = inputViewOf(line + "\n");

        assertThrows(IllegalArgumentException.class, inputView::readVoucher);
    }

    @Test
    @DisplayName("같은 계정 코드 여부는 판단하지 않고 그대로 전달한다")
    void doesNotJudgeSameAccountCode() {
        InputView inputView = inputViewOf("2026-11-06,잘못된 전표,101,101,10000\n");

        VoucherInput input = inputView.readVoucher();

        assertEquals(input.debitCode(), input.creditCode());
    }

    @Test
    @DisplayName("전표 입력 중 입력이 끝나면 입력 종료 예외를 던진다")
    void throwsWhenInputEndsWhileReadingVoucher() {
        InputView inputView = inputViewOf("");

        assertThrows(InputEndedException.class, inputView::readVoucher);
    }

    @Test
    @DisplayName("앞뒤 공백을 제거하고 전표번호를 읽는다")
    void readsVoucherNumber() {
        InputView inputView = inputViewOf(" 4 \n");

        assertEquals(4, inputView.readVoucherNumber());
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "04", "abc", "1,2", ""})
    @DisplayName("전표번호 입력이 형식이나 범위에 맞지 않으면 거절한다")
    void rejectsInvalidVoucherNumber(String line) {
        InputView inputView = inputViewOf(line + "\n");

        assertThrows(IllegalArgumentException.class, inputView::readVoucherNumber);
    }

    @Test
    @DisplayName("전표번호 입력 중 입력이 끝나면 입력 종료 예외를 던진다")
    void throwsWhenInputEndsWhileReadingVoucherNumber() {
        InputView inputView = inputViewOf("");

        assertThrows(InputEndedException.class, inputView::readVoucherNumber);
    }

    @Test
    @DisplayName("한 줄을 읽어 원전표번호와 취소일자로 변환한다")
    void readsCancel() {
        InputView inputView = inputViewOf(" 2 , 2026-11-07 \n");

        assertEquals(new CancelInput(2, LocalDate.of(2026, 11, 7)), inputView.readCancel());
    }

    @ParameterizedTest
    @ValueSource(strings = {"2", "2,2026-11-07,", "0,2026-11-07", "02,2026-11-07", "2,2026-02-30", "2,2026-11-7"})
    @DisplayName("취소 입력의 개수, 전표번호 형식, 날짜가 잘못되면 거절한다")
    void rejectsInvalidCancelInput(String line) {
        InputView inputView = inputViewOf(line + "\n");

        assertThrows(IllegalArgumentException.class, inputView::readCancel);
    }

    @Test
    @DisplayName("존재 여부나 취소 가능 여부는 판단하지 않고 그대로 전달한다")
    void doesNotJudgeCancelability() {
        InputView inputView = inputViewOf("999,0001-01-01\n");

        assertEquals(new CancelInput(999, LocalDate.of(1, 1, 1)), inputView.readCancel());
    }

    @Test
    @DisplayName("취소 입력 중 입력이 끝나면 입력 종료 예외를 던진다")
    void throwsWhenInputEndsWhileReadingCancel() {
        InputView inputView = inputViewOf("");

        assertThrows(InputEndedException.class, inputView::readCancel);
    }
}