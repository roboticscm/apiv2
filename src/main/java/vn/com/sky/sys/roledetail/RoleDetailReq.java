package vn.com.sky.sys.roledetail;

import java.util.List;
import lombok.Data;

@Data
public class RoleDetailReq {
    private Long roleId;
    private List<RoleDetailWithControl> roleDetailWithControls;
}
