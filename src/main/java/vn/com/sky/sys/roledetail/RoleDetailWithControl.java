package vn.com.sky.sys.roledetail;

import lombok.Data;

@Data
public class RoleDetailWithControl {
    private Long branchId;
    private String branchName;
    private Long departmentId;
    private String departmentName;
    private Boolean checked;
    private Long menuId;
    private String menuName;
    private Boolean isPrivate;
    private Boolean approve;
    private Integer dataLevel;
    private Long controlId;
    private String controlName;
    private Boolean renderControl;
    private Boolean disableControl;
    private Boolean confirm;
    private Boolean requirePassword;
}
