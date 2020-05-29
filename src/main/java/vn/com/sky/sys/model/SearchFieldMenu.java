package vn.com.sky.sys.model;

import org.springframework.data.relational.core.mapping.Table;

import lombok.Data;
import lombok.EqualsAndHashCode;
import vn.com.sky.base.GenericEntity;

@EqualsAndHashCode(callSuper = false)
@Data
@Table
public class SearchFieldMenu extends GenericEntity {

	private Long menuId;
	private Long searchFieldId;
	private Long counter;
	private Long accessDate;

}