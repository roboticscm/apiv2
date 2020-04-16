package vn.com.sky.sys.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.relational.core.mapping.Table;
import vn.com.sky.base.SortableEntity;

@EqualsAndHashCode(callSuper = false)
@Data
@Table
public class MenuOrg extends SortableEntity {
    private Long menuId;
    private Long orgId;
}
