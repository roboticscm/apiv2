package vn.com.sky.util;

import java.util.Calendar;

public class SDate {

    public static Long now() {
        return Calendar.getInstance().getTimeInMillis();
    }
}
