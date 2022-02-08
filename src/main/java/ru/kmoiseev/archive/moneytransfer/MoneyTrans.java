package ru.kmoiseev.archive.moneytransfer;

import java.math.BigInteger;

/**
 * @author konstantinmoiseev
 * @since 24.01.2022
 */
public interface MoneyTrans {
    boolean createAccount(String account);
    boolean deposit(String toAccount, BigInteger amount);
    boolean transfer(String fromAccount, String toAccount, BigInteger amount);
    BigInteger getAmount(String account);
}
