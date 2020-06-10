package vn.com.sky.task.model;

import java.util.ArrayList;

import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Table;

import lombok.Data;
import lombok.EqualsAndHashCode;
import vn.com.sky.base.GenericEntity;

@EqualsAndHashCode(callSuper = false)
@Data
@Table
public class TskStatusDetail extends GenericEntity {

	private Long taskId;
	private Long statusId;
	private Long verificationId;
	private Long startTime;
	private Long endTime;
	private String note;
	private String assignPosition;
    private Integer submitStatus = 0;
	
	@Transient
	private ArrayList<String> attachFiles;
	@Transient
	private ArrayList<String> removeAttachFiles;
	@Transient
	private ArrayList<String> insertAttachFiles;
	@Transient
	private Boolean completed;
}