package vn.com.sky.util;

import java.util.LinkedHashMap;
import java.util.List;

public class LinkedHashMapUtil {

    public static <T, K> LinkedHashMap<T, K> fromArrayList(List<LinkedHashMap<T, K>> list) {
        var result = new LinkedHashMap<T, K>();

        for (var l : list) {
            for (var key : l.keySet()) {
                result.put(key, l.get(key));
            }
        }

        return result;
    }
}
