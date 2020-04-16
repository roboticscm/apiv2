package vn.com.sky.sys.model;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.relational.core.mapping.Table;
import vn.com.sky.base.SortableEntity;

@EqualsAndHashCode(callSuper = false)
@Data
@Table
public class Language extends SortableEntity {
    @NotBlank(message = "COMMON.MSG.LOCALE_MUST_NOT_EMPTY")
    @Size(min = 1, max = 10, message = "SYS.MSG.LOCALE_MUST_BE_BETWEEN_1_AND_10_CHARS")
    private String locale;

    @NotBlank(message = "COMMON.MSG.NAME_MUST_NOT_EMPTY")
    private String name;
}
