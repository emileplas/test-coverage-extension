package com.brabel.coverage.extension.single.module.sample;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static com.brabel.coverage.extension.single.module.sample.SecondExampleClass.parseDateFormat;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class SecondExampleClassTest {

    /**
     * We only test the happy path so we can run our assertions with the plugin
     */
    @Test
    public void testHappyPath(){
        String input = "01/12/2024";
        String expected = "12/01/2024";

        String output = parseDateFormat(input);

        assertEquals(expected, output);
    }


}
