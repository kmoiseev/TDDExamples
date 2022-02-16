package ru.kmoiseev.archive.moneytransfer.impl.db.common;

import lombok.Value;

/**
 * @author konstantinmoiseev
 * @since 16.02.2022
 */
@Value
public class AmountWithVersion {
    Long amount;
    Long version;
}
