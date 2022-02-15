package ru.kmoiseev.archive.moneytransfer;

/**
 * @author konstantinmoiseev
 * @since 24.01.2022
 */
public interface MoneyTransfer {
    boolean createAccount(String account);
    boolean deposit(String toAccount, Long amount);
    boolean transfer(String fromAccount, String toAccount, Long amount);
    Long getAmount(String account);
    default void start() {}
    default void shutdown() {}
    default void closeConnection() {};
}
