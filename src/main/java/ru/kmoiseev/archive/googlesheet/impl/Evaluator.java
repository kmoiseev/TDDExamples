package ru.kmoiseev.archive.googlesheet.impl;

import lombok.Value;

import java.util.HashSet;
import java.util.Set;

import static java.lang.Long.parseLong;
import static java.util.Objects.isNull;

/**
 * @author konstantinmoiseev
 * @since 25.01.2022
 */
@Value
public class Evaluator {
    CellsStorage cellsStorage;

    public Long evaluate(String address) {
        final Set<String> visited = new HashSet<>();

        try {
            return evaluate(address, visited);
        } catch (CycledCellsException e) {
            return null;
        }
    }

    private Long evaluate(String address, Set<String> visited) {
        if (visited.contains(address)) {
            throw new CycledCellsException();
        }
        visited.add(address);

        final String cellValue = cellsStorage.get(address);
        if (isNull(cellValue)) {
            return 0L;
        }

        final CellType cellType = CellType.getCellType(cellValue);
        switch (cellType) {
            case SUM: {
                final String[] leftRightAddresses = cellValue.replace("=", "").split("\\+");
                final String leftAddress = leftRightAddresses[0];
                final String rightAddress = leftRightAddresses[1];
                return evaluate(leftAddress, visited) + evaluate(rightAddress, visited);
            }
            case NUMBER: {
                return parseLong(cellValue);
            }
            case REFERENCE: {
                return evaluate(cellValue.replace("=", ""), visited);
            }
        }

        throw new IllegalStateException("There must be at least one cell type");
    }

    private static class CycledCellsException extends RuntimeException {}
}
