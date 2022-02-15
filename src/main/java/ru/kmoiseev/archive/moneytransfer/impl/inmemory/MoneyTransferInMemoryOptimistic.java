package ru.kmoiseev.archive.moneytransfer.impl.inmemory;

import ru.kmoiseev.archive.moneytransfer.MoneyTransfer;
import ru.kmoiseev.archive.moneytransfer.impl.common.InputValidator;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import static java.util.Optional.ofNullable;

/**
 * @author konstantinmoiseev
 * @since 24.01.2022
 */
public class MoneyTransferInMemoryOptimistic implements MoneyTransfer {

    private final Map<String,Account> accounts = new ConcurrentHashMap<>();

    private final InputValidator validator = new InputValidator();

    @Override
    public boolean createAccount(String account) {
        if (!validator.checkAccountInput(account)) {
            return false;
        }
        return accounts.putIfAbsent(account, new Account()) == null;
    }

    @Override
    public boolean deposit(String toAccount, Long amount) {
        if (!validator.checkAccountInput(toAccount) || !validator.checkAmountInput(amount)) {
            return false;
        }

        final Account account = accounts.get(toAccount);
        if (account == null) {
            return false;
        }

        account.deposit.addAndGet(amount);

        return true;
    }

    @Override
    public boolean transfer(String fromAccountName, String toAccountName, Long amount) {
        if (!validator.checkAccountInput(fromAccountName) ||
                !validator.checkAccountInput(toAccountName) ||
                !validator.checkAmountInput(amount)) {
            return false;
        }

        if (!accounts.containsKey(fromAccountName) || !accounts.containsKey(toAccountName)) {
            return false;
        }

        final Account fromAccount = accounts.get(fromAccountName);
        final Account toAccount = accounts.get(toAccountName);

        final long moneyBeforeTransfer = fromAccount.deposit.getAndUpdate((savingsOnAccount) ->
                savingsOnAccount >= amount ? savingsOnAccount - amount : savingsOnAccount
        );
        if (moneyBeforeTransfer < amount) {
            return false;
        }

        toAccount.deposit.addAndGet(amount);

        return true;
    }

    @Override
    public Long getAmount(String accountName) {
        if (!validator.checkAccountInput(accountName)) {
            return null;
        }
        final Account account = accounts.get(accountName);
        return ofNullable(account)
                .map(acc -> acc.deposit)
                .map(AtomicLong::get)
                .orElse(null);
    }

    private static class Account {
        private final AtomicLong deposit = new AtomicLong(0L);
    }
}
