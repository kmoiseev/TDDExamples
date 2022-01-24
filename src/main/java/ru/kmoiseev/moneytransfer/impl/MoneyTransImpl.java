package ru.kmoiseev.moneytransfer.impl;

import ru.kmoiseev.moneytransfer.MoneyTrans;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

import static java.math.BigInteger.ZERO;
import static java.util.Objects.nonNull;
import static java.util.Optional.ofNullable;

/**
 * @author konstantinmoiseev
 * @since 24.01.2022
 */
public class MoneyTransImpl implements MoneyTrans {

    private final Map<String,Account> accounts = new HashMap<>();

    private static boolean checkAccountInput(String account) {
        return nonNull(account) && !account.isBlank() && account.length() <= 256;
    }

    private static boolean checkAmountInput(BigInteger amount) {
        return nonNull(amount) && amount.compareTo(ZERO) > 0;
    }

    @Override
    public boolean createAccount(String account) {
        if (!checkAccountInput(account)) {
            return false;
        }
        if (accounts.containsKey(account)) {
            return false;
        }
        accounts.put(account, new Account());
        return true;
    }

    @Override
    public boolean deposit(String toAccount, BigInteger amount) {
        if (!checkAccountInput(toAccount) || !checkAmountInput(amount)) {
            return false;
        }

        final Account account = accounts.get(toAccount);
        if (account == null) {
            return false;
        }

        synchronized (account) {
            account.deposit = account.deposit.add(amount);
        }

        return true;
    }

    @Override
    public boolean transfer(String fromAccountName, String toAccountName, BigInteger amount) {
        if (!checkAccountInput(fromAccountName) || !checkAccountInput(toAccountName) || !checkAmountInput(amount)) {
            return false;
        }

        if (!accounts.containsKey(fromAccountName) || !accounts.containsKey(toAccountName)) {
            return false;
        }

        final Account fromAccount = accounts.get(fromAccountName);
        final Account toAccount = accounts.get(toAccountName);

        final Object lockOne = fromAccountName.compareTo(toAccountName) > 0 ? fromAccount : toAccount;
        final Object lockTwo = fromAccountName.compareTo(toAccountName) > 0 ? toAccount : fromAccount;

        synchronized (lockOne) {
            synchronized (lockTwo) {
                if (fromAccount.deposit.compareTo(amount) < 0) {
                    return false;
                }

                fromAccount.deposit = fromAccount.deposit.subtract(amount);
                toAccount.deposit = toAccount.deposit.add(amount);
            }
        }

        return true;
    }

    @Override
    public BigInteger getAmount(String accountName) {
        if (!checkAccountInput(accountName)) {
            return null;
        }
        final Account account = accounts.get(accountName);
        return ofNullable(account)
                .map(acc -> acc.deposit)
                .orElse(null);
    }

    private static class Account {
        private volatile BigInteger deposit = ZERO;
    }
}
