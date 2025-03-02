package com.brabel.coverage.extension.single.module.sample;

public class SecondExampleClass {

    /**
     * parses a date in the format of DD/MM/YYYY to MM/DD/YYYY
     * @param date
     * @return
     */
    public static String parseDateFormat(String date){
        if (date == null || !date.matches("\\d{2}/\\d{2}/\\d{4}")) {
            throw new IllegalArgumentException("Invalid date format. Expected format is DD/MM/YYYY.");
        }

        String[] parts = date.split("/");
        return parts[1] + "/" + parts[0] + "/" + parts[2];
    }
}
