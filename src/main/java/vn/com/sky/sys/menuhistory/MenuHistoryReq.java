package vn.com.sky.sys.menuhistory;

import lombok.Data;

@Data
public class MenuHistoryReq {
    private String menuPath;
    private Long departmentId;
}
