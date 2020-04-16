package vn.com.sky.sys.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotBlank;

import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Table;

import vn.com.sky.Message;
import vn.com.sky.base.SortableEntity;

@Data
@Table
@EqualsAndHashCode(callSuper = false)
public class Role extends SortableEntity {
    private Long ownerOrgId;
    private String code;
    @NotBlank(message = Message.NAME_IS_REQUIRED)
    private String name;

    @Transient
    private Boolean checked;
}
