package vn.com.sky.sys.assignmentrole;

import java.util.List;
import lombok.Data;
import vn.com.sky.sys.model.HumanOrOrg;
import vn.com.sky.sys.model.Role;

@Data
public class AssignmentRoleReq {
    private List<Role> roles;
    private List<HumanOrOrg> users;
}
