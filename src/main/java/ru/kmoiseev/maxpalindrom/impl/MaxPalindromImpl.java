package ru.kmoiseev.maxpalindrom.impl;

import lombok.Value;
import ru.kmoiseev.maxpalindrom.MaxPalindrom;

import java.util.Objects;

import static java.util.stream.Collectors.toUnmodifiableMap;

/**
 * @author konstantinmoiseev
 * @since 28.01.2022
 */
public class MaxPalindromImpl implements MaxPalindrom {

    @Value
    private static class Acc {
        int count;
        boolean soloPresent;
    }

    @Override
    public Integer calc(String in) {
        if (Objects.isNull(in)) {
            return null;
        }

        final Acc accumulator = in.chars().boxed().collect(toUnmodifiableMap(i -> i, i -> 1, Integer::sum))
                .values()
                .stream().reduce(
                        new Acc(0, false),
                        (acc, i) -> new Acc(acc.count + i / 2 * 2, i % 2 != 0 || acc.soloPresent),
                        (acc1, acc2) -> new Acc(acc1.count + acc2.count, acc1.soloPresent || acc2.soloPresent));

        return accumulator.count + (accumulator.soloPresent ? 1 : 0);
    }
}
