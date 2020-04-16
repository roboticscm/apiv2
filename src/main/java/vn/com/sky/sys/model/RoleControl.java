package vn.com.sky.sys.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.relational.core.mapping.Table;
import vn.com.sky.base.GenericEntity;

@EqualsAndHashCode(callSuper = false)
@Data
@Table
public class RoleControl extends GenericEntity {
    private Long menuControlId;
    private Long roleDetailId;
    private Boolean renderControl = true;
    private Boolean disableControl = false;
    private Boolean confirm = false;
    private Boolean requirePassword = false;
}
