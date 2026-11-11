package org.example.domain.account;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AccountTypeTest {

    @ParameterizedTest
    @CsvSource({"자산,ASSET", "부채,LIABILITY", "수익,REVENUE", "비용,EXPENSE"})
    @DisplayName("네 가지 한글 분류명을 계정 분류로 변환한다")
    void from_validName(String name, AccountType expected) {
        assertEquals(expected, AccountType.from(name));
    }

    @ParameterizedTest
    @ValueSource(strings = {"자본", "", "ASSET"})
    @DisplayName("허용되지 않은 분류명은 거절한다")
    void from_invalidName(String name) {
        assertThrows(IllegalArgumentException.class, () -> AccountType.from(name));
    }

    @ParameterizedTest
    @CsvSource({
            // 분류,     차변 누계, 대변 누계, 잔액
            "ASSET,     30000,     10000,     20000",   // 자산: 차변이 증가
            "EXPENSE,   30000,     10000,     20000",   // 비용: 차변이 증가
            "LIABILITY, 10000,     30000,     20000",   // 부채: 대변이 증가
            "REVENUE,   10000,     30000,     20000"    // 수익: 대변이 증가
    })
    @DisplayName("분류별로 증가하는 쪽 누계에서 감소하는 쪽 누계를 빼서 잔액을 계산한다")
    void balanceOf(AccountType type, long debitTotal, long creditTotal, long expected) {
        assertEquals(expected, type.balanceOf(debitTotal, creditTotal));
    }

    @ParameterizedTest
    @CsvSource({
            "ASSET,     10000, 30000, -20000",
            "EXPENSE,   10000, 30000, -20000",
            "LIABILITY, 30000, 10000, -20000",
            "REVENUE,   30000, 10000, -20000"
    })
    @DisplayName("감소하는 쪽 누계가 더 크면 음수 잔액을 그대로 돌려준다")
    void balanceOf_negative(AccountType type, long debitTotal, long creditTotal, long expected) {
        assertEquals(expected, type.balanceOf(debitTotal, creditTotal));
    }

    @ParameterizedTest
    @CsvSource({"ASSET", "LIABILITY", "REVENUE", "EXPENSE"})
    @DisplayName("거래가 없으면 잔액은 0원이다")
    void balanceOf_noEntries(AccountType type) {
        assertEquals(0L, type.balanceOf(0L, 0L));
    }

    @ParameterizedTest
    @CsvSource({
            "ASSET,     3000000000, 0,          3000000000",
            "LIABILITY, 0,          3000000000, 3000000000"
    })
    @DisplayName("int 범위를 넘는 누계도 정확하게 계산한다")
    void balanceOf_beyondIntRange(AccountType type, long debitTotal, long creditTotal, long expected) {
        assertEquals(expected, type.balanceOf(debitTotal, creditTotal));
    }
}