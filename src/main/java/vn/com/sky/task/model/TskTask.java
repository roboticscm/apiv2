package vn.com.sky.task.model;

import org.springframework.data.relational.core.mapping.Table;

import lombok.Data;
import lombok.EqualsAndHashCode;
import vn.com.sky.base.GenericEntity;

@EqualsAndHashCode(callSuper = false)
@Data
@Table
public class TskTask extends GenericEntity {
	private Long projectId;
	private String name;
	private String description;
	private Boolean isPrivate = false;
	private Long priorityId;
	private Long lastStatusId;
	private Long startTime;
	private Long deadline;
	private Long firstReminder;
	private Boolean isFirstRemindered = false;
	private Long secondReminder;
	private Boolean isSecondRemindered = false;
	private Long assigneeStartTime;
	private Boolean assigneeStartConfirm = false;
	private Long assigneeEndTime;
	private Boolean assigneeEndConfirm = false;
	private Long evaluateTime;
	private String evaluateComment;
	private Long evaluateQualificationId;
	private Long evaluateVerificationId;
	private Long evaluateStatusId;
    private Integer submitStatus = 0;

}