package com.brabel.coverage.extension.single.module.sample;

public class FirstExampleClass {

    /**
     * Takes a string and set the input to upper case
     * @param input string to set to capital
     * @return the string in capital letters
     */
    public static String stringToCapital(String input){
        return input.toUpperCase();
    }

    /**
     * Method takes a number as a string, increases it by one, parses it back to string.
     * @param numbersAsStringArray string array containing numbers
     * @throws RuntimeException if a string cannot be parsed to a number
     * @return a new string array with each number incremented by one
     */
    public static String[] addOneToNumbersAndReturnStringArray(String[] numbersAsStringArray) {
        String[] resultArray = new String[numbersAsStringArray.length];

        for (int i = 0; i < numbersAsStringArray.length; i++) {
            try {
                int number = Integer.parseInt(numbersAsStringArray[i]);
                resultArray[i] = String.valueOf(number + 1);
            } catch (NumberFormatException e) {
                throw new RuntimeException("Unable to parse the number: " + numbersAsStringArray[i], e);
            }
        }

        return resultArray;
    }
}
