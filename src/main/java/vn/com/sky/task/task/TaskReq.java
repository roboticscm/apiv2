package vn.com.sky.task.task;

import java.util.ArrayList;

import org.springframework.data.annotation.Transient;

import lombok.Data;
import vn.com.sky.task.model.TskStatusDetail;
import vn.com.sky.task.model.TskTask;

@Data
public class TaskReq extends TskTask {
	@Transient
	private ArrayList<String> removeTaskAttachFiles;
	@Transient
	private ArrayList<String> insertTaskAttachFiles;
	
	@Transient
	private ArrayList<Long> removeAssigners;
	@Transient
	private ArrayList<Long> insertAssigners;
	
	@Transient
	private ArrayList<Long> removeAssignees;
	@Transient
	private ArrayList<Long> insertAssignees;
	
	@Transient
	private ArrayList<Long> removeEvaluators;
	@Transient
	private ArrayList<Long> insertEvaluators;
	
	@Transient
	private ArrayList<Long> removeChars;
	@Transient
	private ArrayList<Long> insertChars;
	
	@Transient
	private ArrayList<Long> removeTargetPersons;
	@Transient
	private ArrayList<Long> insertTargetPersons;
	
	@Transient
	private ArrayList<Long> removeTargetTeams;
	@Transient
	private ArrayList<Long> insertTargetTeams;
	
	
	@Transient
	private ArrayList<TskStatusDetail> removeAssignerStatusDetails;
	@Transient
	private ArrayList<TskStatusDetail> insertAssignerStatusDetails;
	@Transient
	private ArrayList<TskStatusDetail> editAssignerStatusDetails;
}
