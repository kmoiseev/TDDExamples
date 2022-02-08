package ru.kmoiseev.archive.googlesheet.impl;

import lombok.experimental.FieldDefaults;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import static lombok.AccessLevel.PRIVATE;

/**
 * @author konstantinmoiseev
 * @since 25.01.2022
 */
@FieldDefaults(makeFinal = true, level = PRIVATE)
public enum CellType {
    NUMBER("^-?\\d{1,19}$"),
    REFERENCE("^\\=[a-zA-Z0-9]{1,256}$"),
    SUM("^\\=[a-zA-Z0-9]{1,256}\\+[a-zA-Z0-9]{1,256}$");

    private final static List<CellType> allTypes = Arrays.asList(CellType.values());
    Pattern validationRegexp;

    CellType(String regexpPattern) {
        this.validationRegexp = Pattern.compile(regexpPattern);
    }

    public static CellType getCellType(String value) {
        if (value == null) {
            return null;
        }
        return allTypes.stream()
                .filter(cellType -> cellType.validationRegexp.matcher(value).matches())
                .findFirst()
                .orElse(null);
    }
}
