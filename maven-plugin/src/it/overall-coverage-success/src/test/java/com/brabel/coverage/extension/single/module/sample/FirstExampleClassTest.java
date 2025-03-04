package com.brabel.coverage.extension.single.module.sample;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static com.brabel.coverage.extension.single.module.sample.FirstExampleClass.addOneToNumbersAndReturnStringArray;
import static com.brabel.coverage.extension.single.module.sample.FirstExampleClass.stringToCapital;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class FirstExampleClassTest {

    @Test
    public void testCapitalLetters(){
        String input ="Hello world!";
        String expected="HELLO WORLD!";

        String output = stringToCapital(input);

        assertEquals(expected, output);
    }

    /**
     * We don't test the number format exception e.g. so we can assert the output from jacoco with our plugin
     */
    @Test
    public void testOnlyHappyPathOfAddOneToNumber(){
        String[] input = new String[]{"1", "2", "3"};
        String[] expected = new String[]{"2", "3", "4"};

        String[] output = addOneToNumbersAndReturnStringArray(input);

        assertArrayEquals(expected, output);
    }
}
