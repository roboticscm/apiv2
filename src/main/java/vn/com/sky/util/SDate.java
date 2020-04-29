package vn.com.sky.util;

import java.util.Calendar;

public class SDate {

    public static Long now() {
        return Calendar.getInstance().getTimeInMillis();
    }
    
    public static Long nowAfter(int numDay) {
        return Calendar.getInstance().getTimeInMillis() + numDay*24*60*60*1000;
    }
}
