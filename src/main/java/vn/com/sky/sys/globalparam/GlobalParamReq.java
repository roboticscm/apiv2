package vn.com.sky.sys.globalparam;

import java.util.List;
import lombok.Data;

@Data
public class GlobalParamReq {
    private String menuPath;
    private String controlId;
    private List<String> keys;
    private List<String> values;

    public String getValue(String key) {
        var index = keys.indexOf(key);

        if (index >= 0) {
            return values.get(index);
        } else {
            return null;
        }
    }
}
