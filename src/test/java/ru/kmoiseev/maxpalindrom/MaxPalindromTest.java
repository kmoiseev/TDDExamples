package ru.kmoiseev.maxpalindrom;

import lombok.Value;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import ru.kmoiseev.maxpalindrom.impl.MaxPalindromImpl;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author konstantinmoiseev
 * @since 28.01.2022
 */
public class MaxPalindromTest {

    private MaxPalindrom maxPalindrom;

    @BeforeEach
    void before() {
        maxPalindrom = new MaxPalindromImpl();
    }


    @Value
    private static class TestCase {
        String in;
        Integer expectedLength;
    }

    private static Stream<TestCase> getTestCases() {
        return Stream.of(
                new TestCase("abbcc", 5),
                new TestCase("abcdefghijk", 1),
                new TestCase("aabbccdefg", 7),
                new TestCase("xxxyyyzzz", 7),
                new TestCase(null, null),
                new TestCase("01234563456", 9)
        );
    }

    @ParameterizedTest
    @MethodSource("getTestCases")
    void testDifferentInputs(final TestCase testCase) {
        assertEquals(testCase.expectedLength, maxPalindrom.calc(testCase.in));
    }
}
