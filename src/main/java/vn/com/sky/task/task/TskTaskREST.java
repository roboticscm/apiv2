package vn.com.sky.task.task;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.POST;
import static org.springframework.web.reactive.function.server.RequestPredicates.PUT;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;
import static org.springframework.web.reactive.function.server.ServerResponse.badRequest;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import lombok.AllArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import vn.com.sky.Constants;
import vn.com.sky.Message;
import vn.com.sky.base.GenericREST;
import vn.com.sky.task.model.AssignPosition;
import vn.com.sky.task.model.TskAssignHumanOrOrg;
import vn.com.sky.task.model.TskAssignOwnerOrg;
import vn.com.sky.task.model.TskStatusAttachFile;
import vn.com.sky.task.model.TskStatusDetail;
import vn.com.sky.task.model.TskTask;
import vn.com.sky.task.model.TskTaskAttachFile;
import vn.com.sky.util.LinkedHashMapUtil;
import vn.com.sky.util.MyServerResponse;

@Configuration
@AllArgsConstructor
public class TskTaskREST extends GenericREST {
    private TskTaskRepo mainRepo;
    private CustomTaskRepo customRepo;
    private TskTaskAttachFileRepo attachFileRepo;
    private TskAssignHumanOrOrgRepo assignHumanRepo;
    private TskAssignOwnerOrgRepo assignOwnerOrgRepo;
    private TskStatusDetailRepo statusDetailRepo;
    private TskStatusAttachFileRepo statusAttachFileRepo;

    @Bean
    public RouterFunction<?> tskTaskRoutes() {
        return route(POST(buildURL(Constants.API_TASK_PREFIX, "task", this::saveOrUpdate)), this::saveOrUpdate)
        		.andRoute(PUT(buildURL(Constants.API_TASK_PREFIX, "task", this::submitOrCancelSubmit)), this::submitOrCancelSubmit)
        		.andRoute(GET(buildURL(Constants.API_TASK_PREFIX, "task", this::tskFindTasks)), this::tskFindTasks)
        		.andRoute(GET(buildURL(Constants.API_TASK_PREFIX, "task", this::tskGetTaskById)), this::tskGetTaskById);
    }
    
    
    private Mono<ServerResponse> tskFindTasks(ServerRequest request) {
        // SYSTEM BLOCK CODE
        // PLEASE DO NOT EDIT
        if (request == null) {
            String methodName = new Object() {}.getClass().getEnclosingMethod().getName();
            return Mono.just(new MyServerResponse(methodName));
        }
        // END SYSTEM BLOCK CODE

        Long page, pageSize;
        try {
            page = getLongParam(request, "page", 1L);
            pageSize = getLongParam(request, "pageSize", -1L);
        } catch (Exception e) {
            return badRequest().bodyValue(Message.INVALID_PAGINATION);
        }
        
        var menuPath = getParam(request, "menuPath");
        
        Long departmentId = null;
		try {
			departmentId = getLongParam(request, "departmentId");
		} catch (Exception e1) {
			return badRequest().bodyValue(Message.INVALID_ID);
		}
        
		var textSearch = getParam(request, "textSearch");
		var taskName = getParam(request, "taskName");
		var projectName = getParam(request, "projectName");
		var assigneeName = getParam(request, "assigneeName");
		var assignerName = getParam(request, "assignerName");
		var evaluatorName = getParam(request, "evaluatorName");
		Boolean isCompleted = null, isDelayDeadline = null, isAssignee = null, isAssigner = null, isEvaluator = null, isExactly = false;
		Long createdDateFrom = null, createdDateTo = null, startTimeFrom = null, startTimeTo = null, deadlineFrom = null, deadlineTo = null;
		try {
			isExactly = getBoolParam(request, "isExactly", false);
			
			isCompleted = getBoolParam(request, "isCompleted");
			isDelayDeadline = getBoolParam(request, "isDelayDeadline");
			
			isAssignee = getBoolParam(request, "isAssignee");
			isAssigner = getBoolParam(request, "isAssigner");
			isEvaluator = getBoolParam(request, "isEvaluator");
			
			createdDateFrom = getLongParam(request, "createdDateFrom");
			createdDateTo = getLongParam(request, "createdDateTo");
			
			startTimeFrom = getLongParam(request, "startTimeFrom");
			startTimeTo = getLongParam(request, "startTimeTo");
			
			deadlineFrom = getLongParam(request, "deadlineFrom");
			deadlineTo = getLongParam(request, "deadlineTo");
			
		} catch (Exception e) {
			e.printStackTrace();
		}
				
	
	
        try {
            return customRepo
                .tskFindTasks(getUserId(request), menuPath, departmentId, page, pageSize,
                		textSearch, 
                		isExactly,
                		taskName,
                		projectName,
                		assigneeName,
                		assignerName,
                		evaluatorName,
                		isCompleted,
                		isDelayDeadline,
                		createdDateFrom,
                		createdDateTo,
                		startTimeFrom,
                		startTimeTo,
                		deadlineFrom,
                		deadlineTo,
                		isAssignee,
                		isAssigner,
                		isEvaluator
                ).flatMap(item -> ok(item))
                .onErrorResume(e -> error(e));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    
    
    private Mono<ServerResponse> tskGetTaskById(ServerRequest request) {
        // SYSTEM BLOCK CODE
        // PLEASE DO NOT EDIT
        if (request == null) {
            String methodName = new Object() {}.getClass().getEnclosingMethod().getName();
            return Mono.just(new MyServerResponse(methodName));
        }
        // END SYSTEM BLOCK CODE

        Long id = null;
		try {
			id = getLongParam(request, "id");
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		
		if(id == null) {
			return error(Message.INVALID_ID);
		}
		
        try {
            return customRepo
                .tskGetTaskById(id)
                .flatMap(item -> ok(item))
                .onErrorResume(e -> error(e));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    
    private Mono<List<LinkedHashMap<String, String>>> validateForSave(TskTask req) {	
        var validateName = mainRepo
            .isNameExisted(req.getName(), req.getProjectId())
            .flatMap(
                existed -> {
                    if (existed) {
                        var serverError = new LinkedHashMap<String, String>();
                        serverError.put("name", "SYS.MSG.NAME_EXISTED");
                        return Mono.just(serverError);
                    }
                    return Mono.empty();
                }
            );

        return Flux.concat(validateName).collectList();
    }
    
    private Mono<List<LinkedHashMap<String, String>>> validateForUpdate(TskTask req) {
        var validateName = mainRepo
            .isNameDuplicated(req.getName(), req.getProjectId(), req.getId())
            .flatMap(
                existed -> {
                    if (existed) {
                        var serverError = new LinkedHashMap<String, String>();
                        serverError.put("name", "SYS.MSG.NAME_EXISTED");
                        return Mono.just(serverError);
                    }
                    return Mono.empty();
                }
            );

        return Flux.concat(validateName).collectList();
    }
    
    
    
    private Mono<ServerResponse> submitOrCancelSubmit(ServerRequest request) {
        // SYSTEM BLOCK CODE
        // PLEASE DO NOT EDIT
        if (request == null) {
            String methodName = new Object() {}.getClass().getEnclosingMethod().getName();
            return Mono.just(new MyServerResponse(methodName));
        }
        // END SYSTEM BLOCK CODE

//		return request.body(BodyExtractors.toFormData()).flatMap(item->{
//        			System.out.println(item);
//        			return ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).bodyValue("OK");
//        		});
        
        return request
                .bodyToMono(TskStatusDetail.class).flatMap(req -> {
                	return statusDetailRepo.findById(req.getId()).flatMap(found -> {
                		found.setStartTime(req.getStartTime());
                		found.setEndTime(req.getEndTime());
                		found.setStatusId(req.getStatusId());
                		found.setVerificationId(req.getVerificationId());
                		found.setNote(req.getNote());
                		
                		
                		
                		if(found.getSubmitStatus() == 1) {
                			found.setSubmitStatus(0);
                		} else {
                			found.setSubmitStatus(1);
                		}
                		
                		return updateEntity(statusDetailRepo, found, getUserId(request)).flatMap(updated -> {
                			Mono<Void> deleteStatusDetailAttachFile$ = Mono.empty();
                			if(req.getRemoveAttachFiles() != null && req.getRemoveAttachFiles().size() > 0) {
                				deleteStatusDetailAttachFile$ = statusAttachFileRepo.deleteByStatusDetailId(updated.getId(), req.getRemoveAttachFiles());
                			}
                			
                			var saveStatusDetailAttachFile$ =  saveStatusDetailAttachFile (getUserId(request), req.getId(), req.getInsertAttachFiles());
                			
                			return Flux.concat(deleteStatusDetailAttachFile$, saveStatusDetailAttachFile$).collectList()
                					.then(updateTaskAccessDate(getUserId(request), req.getTaskId()))
                					.map(res -> updated).flatMap(res -> ok(res, TskStatusDetail.class));
                		});
                	}).switchIfEmpty(updateTaskAccessDate(getUserId(request), req.getTaskId())
                			.then(saveAndSubmitStatusDetail(getUserId(request), req))
                			.flatMap(res -> ok(res, TskStatusDetail.class))
                	);
                });
    }
  
    private Mono<TskTask> updateTaskAccessDate(Long userId, Long taskId) {
    	return mainRepo.findById(taskId).flatMap(found -> {
    		return updateEntity(mainRepo, found, userId);
    	});
    }

    private Mono<ServerResponse> saveOrUpdate(ServerRequest request) {
        // SYSTEM BLOCK CODE
        // PLEASE DO NOT EDIT
        if (request == null) {
            String methodName = new Object() {}.getClass().getEnclosingMethod().getName();
            return Mono.just(new MyServerResponse(methodName));
        }
        // END SYSTEM BLOCK CODE

        return request
                .bodyToMono(TaskReq.class)
                .flatMap(
                    req -> {
                    	
                    	req.setIsFirstRemindered(false);
                    	req.setIsSecondRemindered(false);
                    	
                    	
                        // client validation
                        var clientErrors = validate(req);
                        if (clientErrors != null) return clientErrors;

                        if (req.getId() == null) { // save
                            return validateForSave(req)
                                .flatMap(
                                    errs -> {
                                        if (errs.size() > 0) {
                                            return error(LinkedHashMapUtil.fromArrayList(errs));
                                        } else {
                                            return saveEntity(mainRepo, req, getUserId(request)).flatMap(savedTask -> {
                                            	return saveRelation(getUserId(request), savedTask, req);
                                           
                                            });
                                        }
                                    }
                                );
                        } else { // update
                            return validateForUpdate(req)
                                .flatMap(
                                    errs -> {
                                        if (errs.size() > 0) {
                                            return error(LinkedHashMapUtil.fromArrayList(errs));
                                        } else {
                                            return updateEntity(mainRepo, req, getUserId(request)).flatMap(updatedTask -> {
                                            	return deleteRelation(updatedTask, req).flatMap((e) -> {
                                            		return saveRelation(getUserId(request), updatedTask, req);
                                            	});
                                            });
                                        }
                                    }
                                );
                        }
                    }
                );
    }
    
    private Mono<ServerResponse> saveRelation (Long userId, TskTask task, TaskReq req) {
    	// Task attach file
    	var taskAttachFile$  = saveTaskAttachFile(userId, task.getId(), req.getInsertTaskAttachFiles());
    	
    	// Assign assigner
    	var assigAssigner$ = saveAssignHumanOrOrg(userId, task.getId(), AssignPosition.ASSIGNER.toString(), req.getInsertAssigners());
    	
    	// Assign assignee
    	var assignAssignee$ = saveAssignHumanOrOrg(userId, task.getId(), AssignPosition.ASSIGNEE.toString(), req.getInsertAssignees());
    	
    	
    	// Assign evaluator
    	var assignEvaluator$ = saveAssignHumanOrOrg(userId, task.getId(), AssignPosition.EVALUATOR.toString(), req.getInsertEvaluators());
    	
    	// Assign characteristic task
    	var assignChar$ = saveAssignOwnerOrg(userId, task.getId(), AssignPosition.CHAR.toString(), req.getInsertChars());
    	
    	
    	// Assign Target Person
    	var assignTargetPerson$ = saveAssignHumanOrOrg(userId, task.getId(), AssignPosition.TARGET_PERSON.toString(), req.getInsertTargetPersons());
    	
    	// Assign Target Team
    	var assignTargetTeam$ = saveAssignOwnerOrg(userId, task.getId(), AssignPosition.TARGET_TEAM.toString(), req.getInsertTargetTeams());
    	
    	// Assignee status detail 
    	var assigneeStatusDetail$ = saveStatusDetail(userId, task.getId(), AssignPosition.ASSIGNEE.toString(), req.getInsertAssigneeStatusDetails());
    	
    	// Assigner status detail 
    	var assignerStatusDetail$ = saveStatusDetail(userId, task.getId(), AssignPosition.ASSIGNER.toString(), req.getInsertAssignerStatusDetails());
    	
    	// Update assigner status detail 
    	var updateSssigneeStatusDetail$ = Flux.empty();
    	if(req.getEditAssigneeStatusDetails().size() > 0) {
    		updateSssigneeStatusDetail$ = updateStatusDetail(userId, req.getEditAssigneeStatusDetails());
        	
    	}
    	
    	// Update assigner status detail 
    	var updateSssignerStatusDetail$ = Flux.empty();
    	if(req.getEditAssignerStatusDetails().size() > 0) {
    		updateSssignerStatusDetail$ = updateStatusDetail(userId,  req.getEditAssignerStatusDetails());
        	
    	}
    	
    	return Flux.concat(taskAttachFile$, assigAssigner$, assignAssignee$, assignEvaluator$, assignChar$, assignTargetPerson$, assignTargetTeam$, assigneeStatusDetail$, assignerStatusDetail$, updateSssigneeStatusDetail$, updateSssignerStatusDetail$).collectList().flatMap(list -> {
    		return ok(task, TskTask.class);
    	});
    }
    
    private Mono<List<Void>> deleteRelation (TskTask task, TaskReq req) {
    	// Task attach file
    	Mono<Void> taskAttachFile$ = Mono.empty();
    	if(req.getRemoveTaskAttachFiles().size() > 0) {
    		taskAttachFile$  = attachFileRepo.deleteByTaskId(task.getId(), req.getRemoveTaskAttachFiles());
    	}
    	
    	// Assign Assigner
    	Mono<Void> assignAssigner$ = Mono.empty();
    	if(req.getRemoveAssigners().size() > 0) {
    		assignAssigner$  = assignHumanRepo.deleteByTaskIdAndPosition(task.getId(), AssignPosition.ASSIGNER.toString(), req.getRemoveAssigners());
    	}
    	
    	// Assign Assignee
    	Mono<Void> assignAssignee$ = Mono.empty();
    	if(req.getRemoveAssignees().size() > 0) {
    		assignAssignee$  = assignHumanRepo.deleteByTaskIdAndPosition(task.getId(), AssignPosition.ASSIGNEE.toString(), req.getRemoveAssignees());
    	}
    	
    	
    	// Assign Evaluator
    	Mono<Void> assignEvaluator$ = Mono.empty();
    	if(req.getRemoveEvaluators().size() > 0) {
    		assignEvaluator$  = assignHumanRepo.deleteByTaskIdAndPosition(task.getId(), AssignPosition.EVALUATOR.toString(), req.getRemoveEvaluators());
    	}
    	
    	// Assign Char
    	Mono<Void> assignChar$ = Mono.empty();
    	if(req.getRemoveChars().size() > 0) {
    		assignChar$  = assignOwnerOrgRepo.deleteByTaskIdAndPosition(task.getId(), AssignPosition.CHAR.toString(), req.getRemoveChars());
    	}
    	
    	
    	// Assign Target Person
    	Mono<Void> assignTargetPerson$ = Mono.empty();
    	if(req.getRemoveTargetPersons().size() > 0) {
    		assignTargetPerson$  = assignHumanRepo.deleteByTaskIdAndPosition(task.getId(), AssignPosition.TARGET_PERSON.toString(), req.getRemoveTargetPersons());
    	}
    	
    	// Assign Target Team
    	Mono<Void> assignTargetTeam$ = Mono.empty();
    	if(req.getRemoveTargetTeams().size() > 0) {
    		assignTargetTeam$  = assignOwnerOrgRepo.deleteByTaskIdAndPosition(task.getId(), AssignPosition.TARGET_TEAM.toString(), req.getRemoveTargetTeams());
    	}
    	
    	// Assignee status detail
    	Mono<Void> assigneeStatusDetail$ = Mono.empty();
    	if(req.getRemoveAssigneeStatusDetails().size() > 0) {
    		var ids = req.getRemoveAssigneeStatusDetails().stream().map(it -> it.getId()).collect(Collectors.toList());
    		assigneeStatusDetail$  = statusDetailRepo.deleteByIds(ids);
    	}
    	
    	// Assigner status detail
    	Mono<Void> assignerStatusDetail$ = Mono.empty();
    	if(req.getRemoveAssignerStatusDetails().size() > 0) {
    		var ids = req.getRemoveAssignerStatusDetails().stream().map(it -> it.getId()).collect(Collectors.toList());
    		assignerStatusDetail$  = statusDetailRepo.deleteByIds(ids);
    	}
    	
    	return Flux.concat(taskAttachFile$, assignAssigner$, assignAssignee$, assignEvaluator$, assignChar$, assignTargetPerson$, assignTargetTeam$, assigneeStatusDetail$, assignerStatusDetail$).collectList();
    }
    
    
    private Flux<Object> saveTaskAttachFile (Long userId, Long taskId, ArrayList<String> fileNames) {
    	var entity = new TskTaskAttachFile();
    	entity.setTaskId(taskId);
    	
    	return Flux.fromIterable(fileNames).flatMap(fileName -> {
    		entity.setFileName(fileName);
    		return saveEntity(attachFileRepo, entity, userId);
    	});
    	
    }
    
    
    private Flux<Object> saveAssignHumanOrOrg (Long userId, Long taskId, String assignPosition, ArrayList<Long> humanIds) {
    	var entity = new TskAssignHumanOrOrg();
    	entity.setTaskId(taskId);
    	entity.setAssignPosition(assignPosition);
    	
    	return Flux.fromIterable(humanIds).flatMap(humanId -> {
    		entity.setHumanOrOrgId(humanId);
    		return saveEntity(assignHumanRepo, entity, userId);
    	});
    	
    }
    
    private Flux<Object> saveAssignOwnerOrg (Long userId, Long taskId, String assignPosition, ArrayList<Long> ownerOrgIds) {
    	var entity = new TskAssignOwnerOrg();
    	entity.setTaskId(taskId);
    	entity.setAssignPosition(assignPosition);
    	
    	return Flux.fromIterable(ownerOrgIds).flatMap(ownerOrgId -> {
    		entity.setOwnerOrgId(ownerOrgId);
    		return saveEntity(assignOwnerOrgRepo, entity, userId);
    	});
    	
    }
    
    
    private Flux<Object> saveStatusDetail (Long userId, Long taskId, String assignPosition, ArrayList<TskStatusDetail> statusDetails) {  	
    	return Flux.fromIterable(statusDetails).flatMap(statusDetail -> {
    		statusDetail.setId(null);
    		statusDetail.setTaskId(taskId);
    		statusDetail.setAssignPosition(assignPosition);
    		return saveEntity(statusDetailRepo, statusDetail, userId).flatMapMany(savedStatusDetail -> {
    			return saveStatusDetailAttachFile(userId, savedStatusDetail.getId(), statusDetail.getAttachFiles());
    		});
    	});
    	
    }
    
    
    private Mono<TskStatusDetail> saveAndSubmitStatusDetail (Long userId, TskStatusDetail statusDetail) {  	
    	return Mono.defer(() -> {
    		statusDetail.setId(null);
    		statusDetail.setSubmitStatus(1);
    		return saveEntity(statusDetailRepo, statusDetail, userId).flatMap(savedStatusDetail -> {
    			return saveStatusDetailAttachFile(userId, savedStatusDetail.getId(), statusDetail.getAttachFiles()).collectList().map(res -> savedStatusDetail);
    		});
    	});
    }
    
    private Flux<Object> saveStatusDetailAttachFile (Long userId, Long statusDetailId, ArrayList<String> fileNames) {
    	if(fileNames == null || fileNames.size() == 0) {
    		return Flux.empty();
    	}
    	
    	var entity = new TskStatusAttachFile();
    	entity.setStatusDetailId(statusDetailId);
    	
    	return Flux.fromIterable(fileNames).flatMap(fileName -> {
    		entity.setFileName(fileName);
    		return saveEntity(statusAttachFileRepo, entity, userId);
    	});
    	
    }
    
    
    private Flux<Object> updateStatusDetail (Long userId, ArrayList<TskStatusDetail> statusDetails) {  	
    	return Flux.fromIterable(statusDetails).flatMap(statusDetail -> {
    		return statusDetailRepo.findById(statusDetail.getId()).flatMap(found -> {
    			found.setStartTime(statusDetail.getStartTime());
    			found.setEndTime(statusDetail.getEndTime());
    			
    			if(statusDetail.getStatusId() != null) {
    				found.setStatusId(statusDetail.getStatusId());
    			}
    			
    			if(statusDetail.getVerificationId() !=null) {
    				found.setVerificationId(statusDetail.getVerificationId());
    			}
    			
    			
    			found.setNote(statusDetail.getNote());
    			
    			return updateEntity(statusDetailRepo, found, userId).flatMap(updated -> {
    				
    				Mono<Void> deleteStatusAttachFile$ = Mono.empty();
    				if(statusDetail.getRemoveAttachFiles().size() > 0) {
    					deleteStatusAttachFile$  = statusAttachFileRepo.deleteByStatusDetailId(updated.getId(), statusDetail.getRemoveAttachFiles());
    		    	}
    				
    				var saveStatusAttachFile$ = Flux.empty();
    				if (statusDetail.getInsertAttachFiles().size() > 0) {
    					saveStatusAttachFile$ = saveStatusDetailAttachFile(userId, updated.getId(), statusDetail.getInsertAttachFiles());
    				}
    				
    				return Flux.concat(deleteStatusAttachFile$, saveStatusAttachFile$).collectList();
    			});
    			
    		});
    		
    	});
    	
    }
}
