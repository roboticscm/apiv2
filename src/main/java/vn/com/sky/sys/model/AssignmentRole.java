package vn.com.sky.sys.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.relational.core.mapping.Table;
import vn.com.sky.base.GenericEntity;

@Data
@Table
@EqualsAndHashCode(callSuper = false)
public class AssignmentRole extends GenericEntity {
    private Long roleId;
    private Long userId;
}
