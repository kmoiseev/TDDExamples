package ru.kmoiseev.archive.googlesheet;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.kmoiseev.archive.googlesheet.impl.GoogleSheetImpl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author konstantinmoiseev
 * @since 25.01.2022
 */
public class GoogleSheetTest {
    private GoogleSheet googleSheet;

    @BeforeEach
    void beforeEach() {
        googleSheet = new GoogleSheetImpl();
    }

    // --- PUTTING VALID/INVALID VALUE ---

    @Test
    void canAddACellValuePositiveNumber() {
        assertTrue(googleSheet.putValue("SMTH", "214"));
    }

    @Test
    void canAddACellValueNegativeNumber() {
        assertTrue(googleSheet.putValue("SMTH", "-214"));
    }

    @Test
    void canAddACellValueZeroNumber() {
        assertTrue(googleSheet.putValue("SMTH", "0"));
    }

    @Test
    void cannotAddCellAddressNull() {
        assertFalse(googleSheet.putValue(null, "12"));
    }

    @Test
    void cannotAddCellAddressEmpty() {
        assertFalse(googleSheet.putValue("", "12"));
    }

    @Test
    void cannotAddCellValueEmpty() {
        assertFalse(googleSheet.putValue("ADDR", ""));
    }

    @Test
    void cannotAddCellValueNull() {
        assertFalse(googleSheet.putValue("ADDR", null));
    }

    @Test
    void cannotAddCellValueNaN() {
        assertFalse(googleSheet.putValue("ADDR", "21441AN"));
    }

    // --- PUTTING VALID/INVALID REF VALUE ---

    @Test
    void cannotAddCellRefWithSpace() {
        assertFalse(googleSheet.putValue("ADDR", "=2144 ADF"));
    }

    @Test
    void cannotAddCellRefEmpty() {
        assertFalse(googleSheet.putValue("ADDR", "="));
    }

    @Test
    void canAddCellRefValid() {
        assertTrue(googleSheet.putValue("ADDR", "=ADDRNEXT"));
    }

    // --- PUTTING VALID/INVALID SUM VALUE ---

    @Test
    void cannotAddCellSumWithSpace() {
        assertFalse(googleSheet.putValue("ADDR", "=SDFAS+ ADF"));
    }

    @Test
    void cannotAddCellSumWithEmptyLeft() {
        assertFalse(googleSheet.putValue("ADDR", "=+ADF"));
    }

    @Test
    void cannotAddCellSumWithEmptyRight() {
        assertFalse(googleSheet.putValue("ADDR", "=ADF+"));
    }

    @Test
    void canAddCellSumCorrect() {
        assertTrue(googleSheet.putValue("ADDR", "=ADF+BAD"));
    }

    // --- EVALUATION ---

    @Test
    void nullCellCannotBeEvaluated() {
        assertNull(googleSheet.evaluate(null));
    }

    @Test
    void emptyCellCannotBeEvaluated() {
        assertNull(googleSheet.evaluate(""));
    }

    @Test
    void circularDependentCellsWithRefCannotBeEvaluated() {
        googleSheet.putValue("A1", "=B1");
        googleSheet.putValue("B1", "=C1");
        googleSheet.putValue("C1", "=D1");
        googleSheet.putValue("D1", "=A1");

        assertNull(googleSheet.evaluate("C1"));
    }

    @Test
    void circularDependentCellsWithSumCannotBeEval() {
        googleSheet.putValue("A1", "=B1+C1");
        googleSheet.putValue("B1", "=D1+E1");
        googleSheet.putValue("C1", "=F1+G1");
        googleSheet.putValue("D1", "=1");
        googleSheet.putValue("E1", "=2");
        googleSheet.putValue("F1", "=A1");
        googleSheet.putValue("G1", "=4");

        assertNull(googleSheet.evaluate("A1"));
    }

    @Test
    void notExistingCellEvaluatedAsZero() {
        assertEquals(0L, googleSheet.evaluate("UNKNOWN"));
    }

    @Test
    void cellWithSimpleValueEvaluatedCorrectly() {
        googleSheet.putValue("A1", "23");
        assertEquals(23L, googleSheet.evaluate("A1"));
    }

    @Test
    void cellSumEvalCorrectly() {
        googleSheet.putValue("A1", "=B1+C1");
        googleSheet.putValue("B1", "=D1+E1");
        googleSheet.putValue("C1", "=F1+G1");
        googleSheet.putValue("D1", "1");
        googleSheet.putValue("E1", "2");
        googleSheet.putValue("F1", "3");
        googleSheet.putValue("G1", "4");

        assertEquals(10L, googleSheet.evaluate("A1"));
    }

    @Test
    void cellsWithRefEvalCorrectly() {
        googleSheet.putValue("A1", "=B1");
        googleSheet.putValue("B1", "=C1");
        googleSheet.putValue("C1", "=D1");
        googleSheet.putValue("D1", "-320");

        assertEquals(-320L, googleSheet.evaluate("A1"));
    }

    @Test
    void cellsWithRefAndSumEvalCorrectly() {
        googleSheet.putValue("A1", "=B1+C1");
        googleSheet.putValue("B1", "=D1+E1");
        googleSheet.putValue("C1", "=F1+G1");
        googleSheet.putValue("D1", "1");
        googleSheet.putValue("E1", "2");
        googleSheet.putValue("F1", "3");
        googleSheet.putValue("G1", "=H1");

        googleSheet.putValue("H1", "=H2");
        googleSheet.putValue("H2", "=H3");
        googleSheet.putValue("H3", "=H4");
        googleSheet.putValue("H4", "=H5");
        googleSheet.putValue("H5", "-10");

        assertEquals(-4L, googleSheet.evaluate("A1"));
    }
}
