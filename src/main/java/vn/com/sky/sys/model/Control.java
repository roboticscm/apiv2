package vn.com.sky.sys.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.relational.core.mapping.Table;
import vn.com.sky.base.SortableEntity;

@EqualsAndHashCode(callSuper = false)
@Data
@Table
public class Control extends SortableEntity {
    private String code;
    private String name;
    private Boolean confirm = false;
    private Boolean requirePassword = false;
    private Long sort;
}
