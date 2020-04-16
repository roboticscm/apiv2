package vn.com.sky.sys.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.relational.core.mapping.Table;
import vn.com.sky.base.GenericEntity;

@EqualsAndHashCode(callSuper = false)
@Data
@Table
public class RoleDetail extends GenericEntity {
    private Long roleId;
    private Long menuOrgId;
    private Boolean isPrivate = false;
    private Integer dataLevel;
    private Boolean approve = false;
}
