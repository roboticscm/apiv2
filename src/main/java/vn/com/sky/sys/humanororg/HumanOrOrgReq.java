package vn.com.sky.sys.humanororg;

import java.util.ArrayList;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.annotation.Transient;
import vn.com.sky.sys.model.HumanOrOrg;

@EqualsAndHashCode(callSuper = false)
@Data
public class HumanOrOrgReq extends HumanOrOrg {
    private static final long serialVersionUID = 1L;

    @Transient
    private ArrayList<Long> insertDepartmentIds;

    @Transient
    private ArrayList<Long> removeDepartmentIds;
}
