package com.bogdanorzea.happyhome.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class StringUtils {

    private static final String DATE_FORMAT_FIREBASE = "yyyy-MM-dd'T'HH:mm:sss'Z'";
    public static final String DATA_FORMAT_USER = "dd/MM/yyyy";

    public static String getReadableFormatFromDateString(String isoDateString){
        Date date = null;
        try {
            date = new SimpleDateFormat(DATE_FORMAT_FIREBASE, Locale.US).parse(isoDateString);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        if (date != null) {
            SimpleDateFormat userDisplayFormat = new SimpleDateFormat(DATA_FORMAT_USER, Locale.US);

            return userDisplayFormat.format(date);
        } else {
            return "";
        }
    }

    public static String getIsoFormatFromDateString(String readableDateString){
        Date date = null;
        try {
            date = new SimpleDateFormat(DATA_FORMAT_USER, Locale.US).parse(readableDateString);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        if (date != null) {
            SimpleDateFormat userDisplayFormat = new SimpleDateFormat(DATE_FORMAT_FIREBASE, Locale.US);

            return userDisplayFormat.format(date);
        } else {
            return "";
        }
    }
}
