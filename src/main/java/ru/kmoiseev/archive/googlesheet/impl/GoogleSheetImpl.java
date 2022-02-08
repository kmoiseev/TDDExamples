package ru.kmoiseev.archive.googlesheet.impl;

import ru.kmoiseev.archive.googlesheet.GoogleSheet;

import java.util.HashMap;

import static java.util.Objects.nonNull;

/**
 * @author konstantinmoiseev
 * @since 25.01.2022
 */
public class GoogleSheetImpl implements GoogleSheet {

    private final CellsStorage cellsStorage = new CellsStorage(new HashMap<>());

    private static boolean checkAddress(String address) {
        return nonNull(address) && !address.isBlank() && !address.contains(" ");
    }

    private static boolean checkValue(String value) {
        return CellType.getCellType(value) != null;
    }

    @Override
    public boolean putValue(String address, String value) {
        if (!checkAddress(address)) {
            return false;
        }

        if (!checkValue(value)) {
            return false;
        }

        cellsStorage.put(address, value);
        return true;
    }

    @Override
    public Long evaluate(String address) {
        if (!checkAddress(address)) {
            return null;
        }

        return new Evaluator(cellsStorage).evaluate(address);
    }
}
