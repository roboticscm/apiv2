package vn.com.sky.util;

import java.time.Instant;
import java.time.ZoneId;
import java.util.Calendar;

public class SDate {

    public static Long now() {
    	Instant instant = Instant.now().atZone(ZoneId.of("Asia/Ho_Chi_Minh")).toInstant();
    	return instant.toEpochMilli();
    }
    
    
    public static Long nowAfter(int numDay) {
        return Calendar.getInstance().getTimeInMillis() + numDay*24*60*60*1000;
    }
}
