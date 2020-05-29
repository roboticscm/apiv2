package vn.com.sky.sys.model;

import java.util.ArrayList;

import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Table;

import lombok.Data;
import lombok.EqualsAndHashCode;
import vn.com.sky.base.GenericEntity;

@EqualsAndHashCode(callSuper = false)
@Data
@Table
public class PartNotification extends GenericEntity {

	private Long fromHumanId;
	private Long toHumanId;
	private Long fromGroupId;
	private Long toGroupId;
	private String menuPath;
	private Long departmentId;
	private Long targetId;
	private String type = NotifyType.CHAT.toString();
	private String messageType;
	private String title;
	private Boolean isRead = false;
	private Boolean isFinished = false;
	private String fullTextSearch;
	private Boolean isCancel;
	
	@Transient
	private ArrayList<Long> toHumanListIds;

}