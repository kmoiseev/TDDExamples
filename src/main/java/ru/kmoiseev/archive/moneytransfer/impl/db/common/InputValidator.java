package ru.kmoiseev.archive.moneytransfer.impl.db.common;

import static java.util.Objects.nonNull;

/**
 * @author konstantinmoiseev
 * @since 15.02.2022
 */
public class InputValidator {
    public boolean checkAccountInput(String account) {
        return nonNull(account) && !account.isBlank() && account.length() <= 256;
    }

    public boolean checkAmountInput(Long amount) {
        return nonNull(amount) && amount.compareTo(0L) > 0;
    }
}
