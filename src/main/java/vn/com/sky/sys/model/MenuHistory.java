package vn.com.sky.sys.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.relational.core.mapping.Table;
import vn.com.sky.base.GenericEntity;

@EqualsAndHashCode(callSuper = false)
@Data
@Table
public class MenuHistory extends GenericEntity {
    private Long menuId;
    private Long depId;
    private Long humanId;
    private Long lastAccess;
}
