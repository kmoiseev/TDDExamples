package ru.kmoiseev.googlesheet;

/**
 * @author konstantinmoiseev
 * @since 25.01.2022
 *
 * Supportable values:
 * 1) Just a number, "-23"
 * 2) Sum of two cells, "=A1+A3"
 * 3) Reference to another cell, "=A4"
 */
public interface GoogleSheet {
    boolean putValue(String address, String value);
    Long evaluate(String address);
}
