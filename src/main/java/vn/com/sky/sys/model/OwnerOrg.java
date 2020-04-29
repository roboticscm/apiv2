package vn.com.sky.sys.model;

import javax.validation.constraints.NotBlank;

import org.springframework.data.relational.core.mapping.Table;

import lombok.Data;
import lombok.EqualsAndHashCode;
import vn.com.sky.Message;
import vn.com.sky.base.SortableEntity;

@EqualsAndHashCode(callSuper = false)
@Data
@Table
public class OwnerOrg extends SortableEntity {
    private Long parentId;
    private Integer type;
    private String code;
    @NotBlank(message = Message.NAME_IS_REQUIRED)
    private String name;
    private String slogan;
    private String fontIcon;
    private Boolean useFontIcon;
    private String iconData;
    private String houseNumber;
    private String street;
    private Long wardId;
    private Long districtId;
    private Long cityId;
    private Long countryId;
    private String tel;
    private String email;
    private String facebook;
    private String twitter;
    private String skype;
    private String website;
    private Boolean defaultOrg = false;
}
