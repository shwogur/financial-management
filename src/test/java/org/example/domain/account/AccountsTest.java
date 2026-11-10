package org.example.domain.account;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AccountsTest {

    private Accounts accounts;

    @BeforeEach
    void setUp() {
        accounts = new Accounts();
    }

    private static Account account(String code, String name, AccountType type) {
        return new Account(new AccountCode(code), name, type);
    }

    @Test
    @DisplayName("처음에는 등록된 계정이 없다")
    void emptyAtStart() {
        assertTrue(accounts.findAll().isEmpty());
    }

    @Test
    @DisplayName("네 가지 분류의 계정을 모두 등록한다")
    void registersAllTypes() {
        accounts.register(account("101", "현금", AccountType.ASSET));
        accounts.register(account("201", "차입금", AccountType.LIABILITY));
        accounts.register(account("401", "용역수익", AccountType.REVENUE));
        accounts.register(account("501", "소모품비", AccountType.EXPENSE));

        assertEquals(4, accounts.findAll().size());
    }

    @Test
    @DisplayName("등록 순서와 관계없이 코드 오름차순으로 조회한다")
    void findsAllInCodeOrder() {
        accounts.register(account("501", "소모품비", AccountType.EXPENSE));
        accounts.register(account("001", "현금", AccountType.ASSET));
        accounts.register(account("101", "보통예금", AccountType.ASSET));

        List<String> codes = accounts.findAll().stream()
                .map(account -> account.code().value())
                .toList();

        assertEquals(List.of("001", "101", "501"), codes);
    }

    @Test
    @DisplayName("중복된 코드는 거절하고 기존 목록을 유지한다")
    void rejectsDuplicateCode() {
        accounts.register(account("101", "현금", AccountType.ASSET));

        assertThrows(IllegalArgumentException.class,
                () -> accounts.register(account("101", "보통예금", AccountType.ASSET)));

        List<Account> all = accounts.findAll();
        assertEquals(1, all.size());
        assertEquals("현금", all.get(0).name());
    }

    @Test
    @DisplayName("코드가 다르면 같은 계정명을 허용한다")
    void allowsSameNameWithDifferentCode() {
        accounts.register(account("101", "현금", AccountType.ASSET));
        accounts.register(account("102", "현금", AccountType.ASSET));

        assertEquals(2, accounts.findAll().size());
    }

    @Test
    @DisplayName("조회 결과로 내부 목록을 수정할 수 없다")
    void findAllIsUnmodifiable() {
        List<Account> all = accounts.findAll();

        assertThrows(UnsupportedOperationException.class,
                () -> all.add(account("101", "현금", AccountType.ASSET)));
    }

    @Test
    @DisplayName("코드로 등록된 계정을 찾는다")
    void findsAccountByCode() {
        Account cash = account("101", "현금", AccountType.ASSET);
        accounts.register(cash);
        accounts.register(account("201", "차입금", AccountType.LIABILITY));

        assertEquals(cash, accounts.findByCode(new AccountCode("101")));
    }

    @Test
    @DisplayName("등록되지 않은 코드로 찾으면 거절한다")
    void rejectsUnregisteredCode() {
        accounts.register(account("101", "현금", AccountType.ASSET));

        assertThrows(IllegalArgumentException.class,
                () -> accounts.findByCode(new AccountCode("999")));
    }

    @Test
    @DisplayName("앞자리 0이 다른 코드는 서로 다른 계정으로 찾는다")
    void distinguishesLeadingZero() {
        accounts.register(account("001", "현금", AccountType.ASSET));

        assertThrows(IllegalArgumentException.class,
                () -> accounts.findByCode(new AccountCode("100")));
        assertEquals("현금", accounts.findByCode(new AccountCode("001")).name());
    }

    @Test
    @DisplayName("등록된 계정 수를 알려주고, 등록에 실패하면 수가 늘지 않는다")
    void countsRegisteredAccounts() {
        assertEquals(0, accounts.size());

        accounts.register(account("101", "현금", AccountType.ASSET));
        assertThrows(IllegalArgumentException.class,
                () -> accounts.register(account("101", "보통예금", AccountType.ASSET)));

        assertEquals(1, accounts.size());
    }
}