package vn.com.sky.sys.model;

import javax.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Table;
import vn.com.sky.base.GenericEntity;

@EqualsAndHashCode(callSuper = false)
@Data
@Table
public class Menu extends GenericEntity {
    private String code;

    @NotBlank(message = "COMMON.MSG.MENU_NAME_MUST_NOT_EMPTY")
    private String name;

    private Long sort;
    private String path;
    private String fontIcon;
    private Boolean useFontIcon;
    private String iconData;

    @Transient
    private Boolean restore;

    @Transient
    private Boolean foreverDelete;
}
