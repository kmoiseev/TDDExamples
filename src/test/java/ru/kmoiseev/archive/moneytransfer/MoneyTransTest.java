package ru.kmoiseev.archive.moneytransfer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.kmoiseev.archive.moneytransfer.impl.MoneyTransImpl;

import java.math.BigInteger;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import static java.math.BigInteger.valueOf;
import static java.util.concurrent.CompletableFuture.runAsync;
import static java.util.stream.IntStream.rangeClosed;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author konstantinmoiseev
 * @since 24.01.2022
 */
public class MoneyTransTest {

    private MoneyTrans moneyTrans;

    @BeforeEach
    void beforeEach() {
        moneyTrans = new MoneyTransImpl();
    }

    // ------------------ create --------------------
    @Test
    void canCreateAnAccount() {
        assertTrue(moneyTrans.createAccount("acc"));
    }

    @Test
    void cannotCreateAccountTwice() {
        moneyTrans.createAccount("acc");
        assertFalse(moneyTrans.createAccount("acc"));
    }

    @Test
    void canCreateMultipleDifferentAcc() {
        rangeClosed(1, 1000)
                .forEach(i -> assertTrue(moneyTrans.createAccount("account#" + i)));
    }

    @Test
    void cannotCreateAccountWithNullId() {
        assertFalse(moneyTrans.createAccount(null));
    }

    @Test
    void cannotCreateAccountWithEmptyId() {
        assertFalse(moneyTrans.createAccount(""));
    }

    @Test
    void cannotCreateAccountWithIdMoreThan256Symbols() {
        assertFalse(moneyTrans.createAccount(rangeClosed(1, 257).mapToObj(i -> "i").collect(Collectors.joining(""))));
    }

    @Test
    void createdAccountHasZeroAmount() {
        moneyTrans.createAccount("acc");

        assertEquals(valueOf(0), moneyTrans.getAmount("acc"));
    }

    @Test
    void nonExistingAccountHasNullAmount() {
        assertNull(moneyTrans.getAmount("acc"));
    }

    // ------------------ deposit --------------------

    @Test
    void cannotDepositMoneyToANonExistingAccount() {
        assertFalse(moneyTrans.deposit("unknown", valueOf(10L)));
    }

    @Test
    void cannotDepositToNull() {
        assertFalse(moneyTrans.deposit(null, valueOf(10L)));
    }

    @Test
    void cannotDepositToEmpty() {
        assertFalse(moneyTrans.deposit("", valueOf(10L)));
    }

    @Test
    void cannotDepositNull() {
        moneyTrans.createAccount("acc");
        assertFalse(moneyTrans.deposit("acc", null));
    }

    @Test
    void cannotDepositNegativeAmount() {
        moneyTrans.createAccount("acc");
        assertFalse(moneyTrans.deposit("acc", valueOf(-1L)));
    }

    @Test
    void cannotDepositZeroAmount() {
        moneyTrans.createAccount("acc");
        assertFalse(moneyTrans.deposit("acc", valueOf(0)));
    }

    @Test
    void canDepositWithValidInput() {
        moneyTrans.createAccount("acc");

        assertTrue(moneyTrans.deposit("acc", valueOf(10)));
    }

    @Test
    void validDepositIncreasesMoneyAmount() {
        moneyTrans.createAccount("acc");
        moneyTrans.deposit("acc", valueOf(10));

        assertEquals(valueOf(10), moneyTrans.getAmount("acc"));
    }

    @Test
    void invalidDepositDoesNotIncreaseMoneyAmount() {
        moneyTrans.createAccount("acc");
        moneyTrans.deposit("acc2", valueOf(10));

        assertNull(moneyTrans.getAmount("acc2"));
    }

    // ------------------ transfer --------------------

    @Test
    void cannotTransferFromNull() {
        moneyTrans.createAccount("accFrom");
        moneyTrans.createAccount("accTo");

        assertFalse(moneyTrans.transfer(null, "accTo", valueOf(10)));
    }

    @Test
    void cannotTransferFromEmpty() {
        moneyTrans.createAccount("accFrom");
        moneyTrans.createAccount("accTo");

        assertFalse(moneyTrans.transfer("", "accTo", valueOf(10)));
    }

    @Test
    void cannotTransferFromNotExistingAccount() {
        moneyTrans.createAccount("accTo");

        assertFalse(moneyTrans.transfer("accFrom", "accTo", valueOf(10)));
    }

    @Test
    void cannotTransferToNull() {
        moneyTrans.createAccount("accFrom");
        moneyTrans.createAccount("accTo");

        assertFalse(moneyTrans.transfer("accFrom", null, valueOf(10)));
    }

    @Test
    void cannotTransferToEmpty() {
        moneyTrans.createAccount("accFrom");
        moneyTrans.createAccount("accTo");

        assertFalse(moneyTrans.transfer("accFrom", "", valueOf(10)));
    }

    @Test
    void cannotTransferToNotExistingAccount() {
        moneyTrans.createAccount("accFrom");

        assertFalse(moneyTrans.transfer("accFrom", "accTo", valueOf(10)));
    }

    @Test
    void cannotTransferNullAmount() {
        moneyTrans.createAccount("accFrom");
        moneyTrans.createAccount("accTo");

        assertFalse(moneyTrans.transfer("accFrom", "accTo", null));
    }


    @Test
    void cannotTransferNegativeAmount() {
        moneyTrans.createAccount("accFrom");
        moneyTrans.createAccount("accTo");

        assertFalse(moneyTrans.transfer("accFrom", "accTo", valueOf(-10)));
    }

    @Test
    void cannotTransferZeroAmount() {
        moneyTrans.createAccount("accFrom");
        moneyTrans.createAccount("accTo");

        assertFalse(moneyTrans.transfer("accFrom", "accTo", valueOf(0)));
    }

    @Test
    void cannotTransferWhenInsufficientAmountOnSource() {
        moneyTrans.createAccount("accFrom");
        moneyTrans.createAccount("accTo");

        assertFalse(moneyTrans.transfer("accFrom", "accTo", valueOf(10)));
    }

    @Test
    void canTransferWhenSufficientAmountOnSource() {
        moneyTrans.createAccount("accFrom");
        moneyTrans.createAccount("accTo");

        moneyTrans.deposit("accFrom", valueOf(10));

        assertTrue(moneyTrans.transfer("accFrom", "accTo", valueOf(10)));
        assertEquals(valueOf(0), moneyTrans.getAmount("accFrom"));
        assertEquals(valueOf(10), moneyTrans.getAmount("accTo"));
    }

    // ----------- Async tests -----------
    @Test
    void manyTransfersFromOneAccountToAnotherPlusDeposits() {
        final BigInteger leftAmount = valueOf(1000000);
        final String leftName = "accLeft";
        final BigInteger rightAmount = valueOf(2000000);
        final String rightName = "accRight";

        moneyTrans.createAccount(leftName);
        moneyTrans.deposit(leftName, leftAmount);

        moneyTrans.createAccount(rightName);
        moneyTrans.deposit(rightName, rightAmount);

        // executing in parallel 300k transfers 1$ each from right to left
        // and 200k transfers 1$ each from left to right
        // in parallel, adding 250k deposits to each
        // expecting left to have 1350k and right to have 2150k

        final ExecutorService executorService = Executors.newFixedThreadPool(50);

        // right to left 300k 1$ each
        final CompletableFuture<?> rightToLeftAsync = runAsync(() ->
                        rangeClosed(1, 300000)
                                .mapToObj(i -> runAsync(() ->
                                        moneyTrans.transfer(rightName, leftName, BigInteger.ONE), executorService))
                                .forEach(CompletableFuture::join),
                executorService);

        // right debit 250k 1$ each
        final CompletableFuture<?> rightDepositAsync = runAsync(() ->
                        rangeClosed(1, 250000)
                                .mapToObj(i -> runAsync(() ->
                                        moneyTrans.deposit(rightName, BigInteger.ONE), executorService))
                                .forEach(CompletableFuture::join),
                executorService);

        // left to right 200k 1$ each
        final CompletableFuture<?> leftToRightAsync = runAsync(() ->
                        rangeClosed(1, 200000)
                                .mapToObj(i -> runAsync(() ->
                                        moneyTrans.transfer(leftName, rightName, BigInteger.ONE), executorService))
                                .forEach(CompletableFuture::join)
                , executorService);

        // left debit 250k 1$ each
        final CompletableFuture<?> leftDepositAsync = runAsync(() ->
                        rangeClosed(1, 250000)
                                .mapToObj(i -> runAsync(() ->
                                        moneyTrans.deposit(leftName, BigInteger.ONE), executorService))
                                .forEach(CompletableFuture::join),
                executorService);

        rightToLeftAsync.join();
        leftToRightAsync.join();
        leftDepositAsync.join();
        rightDepositAsync.join();

        assertEquals(valueOf(1350000), moneyTrans.getAmount("accLeft"));
        assertEquals(valueOf(2150000), moneyTrans.getAmount("accRight"));
    }
}
