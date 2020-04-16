package vn.com.sky.sys.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Table;
import vn.com.sky.base.GenericEntity;

@EqualsAndHashCode(callSuper = false)
@Data
@Table
public class MenuControl extends GenericEntity {
    private Long menuId;
    private Long controlId;

    @Transient
    private Boolean checked;

    @Transient
    private String menuPath;
}
