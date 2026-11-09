package org.example.domain;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class Accounts {

    private final Map<AccountCode, Account> accountsByCode = new TreeMap<>();

    public void register(Account account) {
        AccountCode code = account.code();
        if (accountsByCode.containsKey(code)) {
            throw new IllegalArgumentException("이미 등록된 계정 코드입니다. (" + code.value() + ")");
        }
        accountsByCode.put(code, account);
    }

    public List<Account> findAll() {
        return List.copyOf(accountsByCode.values());
    }
}