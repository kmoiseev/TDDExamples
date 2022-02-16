package ru.kmoiseev.archive.moneytransfer;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.stream.Collectors;

import static java.lang.Long.valueOf;
import static java.util.stream.IntStream.rangeClosed;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author konstantinmoiseev
 * @since 24.01.2022
 */
public class MoneyTransferTestAbstract {

    protected MoneyTransfer moneyTransfer;
    private final Long moneyMultiplier;

    public MoneyTransferTestAbstract(MoneyTransfer moneyTransfer, Long moneyMultiplier) {
        this.moneyTransfer = moneyTransfer;
        this.moneyMultiplier = moneyMultiplier;
    }

    @BeforeEach
    void beforeEach() {
        moneyTransfer.start();
    }

    @AfterEach
    void afterEach() {
        moneyTransfer.shutdown();
    }

    // ------------------ create --------------------
    @Test
    void canCreateAnAccount() {
        assertTrue(moneyTransfer.createAccount("acc"));
    }

    @Test
    void cannotCreateAccountTwice() {
        moneyTransfer.createAccount("acc");
        assertFalse(moneyTransfer.createAccount("acc"));
    }

    @Test
    void canCreateMultipleDifferentAcc() {
        rangeClosed(1, 100)
                .forEach(i -> assertTrue(moneyTransfer.createAccount("account#" + i)));
    }

    @Test
    void cannotCreateAccountWithNullId() {
        assertFalse(moneyTransfer.createAccount(null));
    }

    @Test
    void cannotCreateAccountWithEmptyId() {
        assertFalse(moneyTransfer.createAccount(""));
    }

    @Test
    void cannotCreateAccountWithIdMoreThan256Symbols() {
        assertFalse(moneyTransfer.createAccount(rangeClosed(1, 257).mapToObj(i -> "i").collect(Collectors.joining(""))));
    }

    @Test
    void createdAccountHasZeroAmount() {
        moneyTransfer.createAccount("acc");

        assertEquals(valueOf(0), moneyTransfer.getAmount("acc"));
    }

    @Test
    void nonExistingAccountHasNullAmount() {
        assertNull(moneyTransfer.getAmount("acc"));
    }

    // ------------------ deposit --------------------

    @Test
    void cannotDepositMoneyToANonExistingAccount() {
        assertFalse(moneyTransfer.deposit("unknown", 10L));
    }

    @Test
    void cannotDepositToNull() {
        assertFalse(moneyTransfer.deposit(null, 10L));
    }

    @Test
    void cannotDepositToEmpty() {
        assertFalse(moneyTransfer.deposit("", 10L));
    }

    @Test
    void cannotDepositNull() {
        moneyTransfer.createAccount("acc");
        assertFalse(moneyTransfer.deposit("acc", null));
    }

    @Test
    void cannotDepositNegativeAmount() {
        moneyTransfer.createAccount("acc");
        assertFalse(moneyTransfer.deposit("acc", -1L));
    }

    @Test
    void cannotDepositZeroAmount() {
        moneyTransfer.createAccount("acc");
        assertFalse(moneyTransfer.deposit("acc", 0L));
    }

    @Test
    void canDepositWithValidInput() {
        moneyTransfer.createAccount("acc");

        assertTrue(moneyTransfer.deposit("acc", 10L));
    }

    @Test
    void validDepositIncreasesMoneyAmount() {
        moneyTransfer.createAccount("acc");
        moneyTransfer.deposit("acc", 10L);

        assertEquals(valueOf(10), moneyTransfer.getAmount("acc"));
    }

    @Test
    void invalidDepositDoesNotIncreaseMoneyAmount() {
        moneyTransfer.createAccount("acc");
        moneyTransfer.deposit("acc2", 10L);

        assertNull(moneyTransfer.getAmount("acc2"));
    }

    // ------------------ transfer --------------------

    @Test
    void cannotTransferFromNull() {
        moneyTransfer.createAccount("accFrom");
        moneyTransfer.createAccount("accTo");

        assertFalse(moneyTransfer.transfer(null, "accTo", 10L));
    }

    @Test
    void cannotTransferFromEmpty() {
        moneyTransfer.createAccount("accFrom");
        moneyTransfer.createAccount("accTo");

        assertFalse(moneyTransfer.transfer("", "accTo", 10L));
    }

    @Test
    void cannotTransferFromNotExistingAccount() {
        moneyTransfer.createAccount("accTo");

        assertFalse(moneyTransfer.transfer("accFrom", "accTo", 10L));
    }

    @Test
    void cannotTransferToNull() {
        moneyTransfer.createAccount("accFrom");
        moneyTransfer.createAccount("accTo");

        assertFalse(moneyTransfer.transfer("accFrom", null, 10L));
    }

    @Test
    void cannotTransferToEmpty() {
        moneyTransfer.createAccount("accFrom");
        moneyTransfer.createAccount("accTo");

        assertFalse(moneyTransfer.transfer("accFrom", "", 10L));
    }

    @Test
    void cannotTransferToNotExistingAccount() {
        moneyTransfer.createAccount("accFrom");

        assertFalse(moneyTransfer.transfer("accFrom", "accTo", 10L));
    }

    @Test
    void cannotTransferNullAmount() {
        moneyTransfer.createAccount("accFrom");
        moneyTransfer.createAccount("accTo");

        assertFalse(moneyTransfer.transfer("accFrom", "accTo", null));
    }


    @Test
    void cannotTransferNegativeAmount() {
        moneyTransfer.createAccount("accFrom");
        moneyTransfer.createAccount("accTo");

        assertFalse(moneyTransfer.transfer("accFrom", "accTo", (long) -10));
    }

    @Test
    void cannotTransferZeroAmount() {
        moneyTransfer.createAccount("accFrom");
        moneyTransfer.createAccount("accTo");

        assertFalse(moneyTransfer.transfer("accFrom", "accTo", 0L));
    }

    @Test
    void cannotTransferWhenInsufficientAmountOnSource() {
        moneyTransfer.createAccount("accFrom");
        moneyTransfer.createAccount("accTo");

        assertFalse(moneyTransfer.transfer("accFrom", "accTo", 10L));
    }

    @Test
    void canTransferWhenSufficientAmountOnSource() {
        moneyTransfer.createAccount("accFrom");
        moneyTransfer.createAccount("accTo");

        moneyTransfer.deposit("accFrom", 10L);

        assertTrue(moneyTransfer.transfer("accFrom", "accTo", 10L));
        assertEquals(valueOf(0), moneyTransfer.getAmount("accFrom"));
        assertEquals(valueOf(10), moneyTransfer.getAmount("accTo"));
    }

    // ----------- Async tests -----------
    @Test
    void manyTransfersFromOneAccountToAnotherPlusDeposits() {
        final Long leftAmount = 100 * moneyMultiplier;
        final String leftName = "accLeft";
        final Long rightAmount = 200 * moneyMultiplier;
        final String rightName = "accRight";

        moneyTransfer.createAccount(leftName);
        moneyTransfer.deposit(leftName, leftAmount);

        moneyTransfer.createAccount(rightName);
        moneyTransfer.deposit(rightName, rightAmount);

        // executing in parallel 300k transfers 1$ each from right to left
        // and 200k transfers 1$ each from left to right
        // in parallel, adding 250k deposits to each
        // expecting left to have 1350k and right to have 2150k

        // right to left 300k 1$ each
        final Thread rightToLeftAsync = new Thread(() -> {
            for (int i = 0; i < 30 * moneyMultiplier; ++i) {
                moneyTransfer.transfer(rightName, leftName, 1L);
            }
            moneyTransfer.closeConnection();
        });

        // right debit 250k 1$ each
        final Thread rightDepositAsync = new Thread(() -> {
            for (int i = 0; i < 25 * moneyMultiplier; ++i) {
                moneyTransfer.deposit(rightName, 1L);
            }
            moneyTransfer.closeConnection();
        });

        // left to right 200k 1$ each
        final Thread leftToRightAsync = new Thread(() -> {
            for (int i = 0; i < 20 * moneyMultiplier; ++i) {
                moneyTransfer.transfer(leftName, rightName, 1L);
            }
            moneyTransfer.closeConnection();
        });

        // left debit 250k 1$ each
        final Thread leftDepositAsync = new Thread(() -> {
            for (int i = 0; i < 25 * moneyMultiplier; ++i) {
                moneyTransfer.deposit(leftName, 1L);
            }
            moneyTransfer.closeConnection();
        });

        // left to right incorrect
        final Thread leftToRightIncorrectAsync = new Thread(() -> {
            for (int i = 0; i < 30 * moneyMultiplier; ++i) {
                moneyTransfer.transfer(leftName, rightName, 500L * moneyMultiplier);
            }
            moneyTransfer.closeConnection();
        });

        // right to left incorrect
        final Thread rightToLeftIncorrectAsync = new Thread(() -> {
            for (int i = 0; i < 30 * moneyMultiplier; ++i) {
                moneyTransfer.transfer(rightName, leftName, 500L * moneyMultiplier);
            }
            moneyTransfer.closeConnection();
        });

        try {
            rightToLeftAsync.start();
            leftToRightAsync.start();
            leftDepositAsync.start();
            rightDepositAsync.start();
            leftToRightIncorrectAsync.start();
            rightToLeftIncorrectAsync.start();

            rightToLeftAsync.join();
            leftToRightAsync.join();
            leftDepositAsync.join();
            rightDepositAsync.join();
            leftToRightIncorrectAsync.join();
            rightToLeftIncorrectAsync.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        assertEquals(valueOf(135 * moneyMultiplier), moneyTransfer.getAmount("accLeft"));
        assertEquals(valueOf(215 * moneyMultiplier), moneyTransfer.getAmount("accRight"));
    }

    // ----------- Async tests -----------
    void manyTransfersFromOneAccountToAnotherSingleThread() {
        final String accountLeft = "accLeft";
        final String accountRight = "accRight";
        final Long amountLeft = 100L * moneyMultiplier;
        final Long amountRight = 100L * moneyMultiplier;

        moneyTransfer.createAccount(accountLeft);
        moneyTransfer.deposit(accountLeft, amountLeft);
        moneyTransfer.createAccount(accountRight);
        moneyTransfer.deposit(accountRight, amountRight);

        for (int i = 0; i < 100 * moneyMultiplier; ++i) {
            moneyTransfer.transfer(accountLeft, accountRight, 1L);
        }

        assertEquals(valueOf(0L), moneyTransfer.getAmount("accLeft"));
        assertEquals(valueOf(2 * 100L * moneyMultiplier), moneyTransfer.getAmount("accRight"));
    }
}
