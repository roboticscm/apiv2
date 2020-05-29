package vn.com.sky.sys.model;

import org.springframework.data.relational.core.mapping.Table;

import lombok.Data;
import lombok.EqualsAndHashCode;
import vn.com.sky.base.GenericEntity;

@EqualsAndHashCode(callSuper = false)
@Data
@Table
public class SearchField extends GenericEntity {

    private String name;
    private String field;
    private String type;
    private Long accessDate;

}