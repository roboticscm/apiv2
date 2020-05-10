package vn.com.sky.task.model;

import org.springframework.data.relational.core.mapping.Table;

import lombok.Data;
import lombok.EqualsAndHashCode;
import vn.com.sky.base.GenericEntity;

@EqualsAndHashCode(callSuper = false)
@Data
@Table
public class TskAssignOwnerOrg extends GenericEntity {

	private Long taskId;
	private Long ownerOrgId;
	private String assignPosition;

}
