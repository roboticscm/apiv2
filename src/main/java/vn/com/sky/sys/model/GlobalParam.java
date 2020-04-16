package vn.com.sky.sys.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.relational.core.mapping.Table;
import vn.com.sky.base.SortableEntity;

@EqualsAndHashCode(callSuper = false)
@Data
@Table
public class GlobalParam extends SortableEntity {
    private Long companyId;
    private String category;
    private String key;
    private String value;
    private Long sort;
    private Boolean disabled = false;
}
