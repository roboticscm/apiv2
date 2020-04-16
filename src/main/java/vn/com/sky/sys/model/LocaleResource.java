package vn.com.sky.sys.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Table;
import vn.com.sky.base.GenericEntity;

@EqualsAndHashCode(callSuper = false)
@Data
@Table
public class LocaleResource extends GenericEntity {
    private Long companyId;
    private String locale;
    private String category;
    private String typeGroup;
    private String key;
    private String value;

    @Transient
    private String newValue;

    private Boolean disabled;
}
