package ru.kmoiseev.googlesheet.impl;

import lombok.Value;

import java.util.Map;

/**
 * @author konstantinmoiseev
 * @since 25.01.2022
 */
@Value
public class CellsStorage {
    Map<String,String> cells;

    public void put(String address, String value) {
        cells.put(address, value);
    }

    public String get(String address) {
        return cells.get(address);
    }
}
