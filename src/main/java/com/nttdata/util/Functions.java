package com.nttdata.util;

public class Functions {

    public static Double roundToTwoDecimalPlaces(Double value) {
        if (value == null) {
            return null;
        }
        return Math.round(value * 100.0) / 100.0;
    }

}
