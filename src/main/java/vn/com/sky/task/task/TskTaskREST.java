package vn.com.sky.task.task;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.POST;
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
import vn.com.sky.security.AuthenticationManager;
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
    private AuthenticationManager auth;
    private CustomTaskRepo customRepo;
    private TskTaskAttachFileRepo attachFileRepo;
    private TskAssignHumanOrOrgRepo assignHumanRepo;
    private TskAssignOwnerOrgRepo assignOwnerOrgRepo;
    private TskStatusDetailRepo statusDetailRepo;
    private TskStatusAttachFileRepo statusAttachFileRepo;

    @Bean
    public RouterFunction<?> tskTaskRoutes() {
        return route(POST(buildURL(Constants.API_TASK_PREFIX, "task", this::saveOrUpdate)), this::saveOrUpdate)
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
        
        try {
            return customRepo
                .tskFindTasks(auth.getUserId(), menuPath, departmentId, page, pageSize)
                .flatMap(item -> ok(item))
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
                    	System.out.println(req);
                    	
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
                                            return saveEntity(mainRepo, req, auth).flatMap(savedTask -> {
                                            	return saveRelation(savedTask, req);
                                           
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
                                            return updateEntity(mainRepo, req, auth).flatMap(updatedTask -> {
                                            	return deleteRelation(updatedTask, req).flatMap((e) -> {
                                            		return saveRelation(updatedTask, req);
                                            	});
                                            });
                                        }
                                    }
                                );
                        }
                    }
                );
    }
    
    private Mono<ServerResponse> saveRelation (TskTask task, TaskReq req) {
    	// Task attach file
    	var taskAttachFile$  = saveTaskAttachFile(task.getId(), req.getInsertTaskAttachFiles());
    	
    	// Assign assigner
    	var assigAssigner$ = saveAssignHumanOrOrg(task.getId(), AssignPosition.ASSIGNER.toString(), req.getInsertAssigners());
    	
    	// Assign assignee
    	var assignAssignee$ = saveAssignHumanOrOrg(task.getId(), AssignPosition.ASSIGNEE.toString(), req.getInsertAssignees());
    	
    	
    	// Assign evaluator
    	var assignEvaluator$ = saveAssignHumanOrOrg(task.getId(), AssignPosition.EVALUATOR.toString(), req.getInsertEvaluators());
    	
    	// Assign characteristic task
    	var assignChar$ = saveAssignOwnerOrg(task.getId(), AssignPosition.CHAR.toString(), req.getInsertChars());
    	
    	
    	// Assign Target Person
    	var assignTargetPerson$ = saveAssignHumanOrOrg(task.getId(), AssignPosition.TARGET_PERSON.toString(), req.getInsertTargetPersons());
    	
    	// Assign Target Team
    	var assignTargetTeam$ = saveAssignOwnerOrg(task.getId(), AssignPosition.TARGET_TEAM.toString(), req.getInsertTargetTeams());
    	
    	// Assigner status detail 
    	var assignerStatusDetail$ = saveStatusDetail(task.getId(), AssignPosition.ASSIGNER.toString(), req.getInsertAssignerStatusDetails());
    	
    	// Update assigner status detail 
    	var updateSssignerStatusDetail$ = Flux.empty();
    	if(req.getEditAssignerStatusDetails().size() > 0) {
    		updateSssignerStatusDetail$ = updateStatusDetail(req.getEditAssignerStatusDetails());
        	
    	}
    	
    	return Flux.concat(taskAttachFile$, assigAssigner$, assignAssignee$, assignEvaluator$, assignChar$, assignTargetPerson$, assignTargetTeam$, assignerStatusDetail$, updateSssignerStatusDetail$).collectList().flatMap(list -> {
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
    	
    	// Assigner status detail
    	Mono<Void> assignerStatusDetail$ = Mono.empty();
    	if(req.getRemoveAssignerStatusDetails().size() > 0) {
    		var ids = req.getRemoveAssignerStatusDetails().stream().map(it -> it.getId()).collect(Collectors.toList());
    		assignerStatusDetail$  = statusDetailRepo.deleteByIds(ids);
    	}
    	
    	return Flux.concat(taskAttachFile$, assignAssigner$, assignAssignee$, assignEvaluator$, assignChar$, assignTargetPerson$, assignTargetTeam$, assignerStatusDetail$).collectList();
    }
    
    
    private Flux<Object> saveTaskAttachFile (Long taskId, ArrayList<String> fileNames) {
    	var entity = new TskTaskAttachFile();
    	entity.setTaskId(taskId);
    	
    	return Flux.fromIterable(fileNames).flatMap(fileName -> {
    		entity.setFileName(fileName);
    		return saveEntity(attachFileRepo, entity, auth);
    	});
    	
    }
    
    
    private Flux<Object> saveAssignHumanOrOrg (Long taskId, String assignPosition, ArrayList<Long> humanIds) {
    	var entity = new TskAssignHumanOrOrg();
    	entity.setTaskId(taskId);
    	entity.setAssignPosition(assignPosition);
    	
    	return Flux.fromIterable(humanIds).flatMap(humanId -> {
    		entity.setHumanOrOrgId(humanId);
    		return saveEntity(assignHumanRepo, entity, auth);
    	});
    	
    }
    
    private Flux<Object> saveAssignOwnerOrg (Long taskId, String assignPosition, ArrayList<Long> ownerOrgIds) {
    	var entity = new TskAssignOwnerOrg();
    	entity.setTaskId(taskId);
    	entity.setAssignPosition(assignPosition);
    	
    	return Flux.fromIterable(ownerOrgIds).flatMap(ownerOrgId -> {
    		entity.setOwnerOrgId(ownerOrgId);
    		return saveEntity(assignOwnerOrgRepo, entity, auth);
    	});
    	
    }
    
    
    private Flux<Object> saveStatusDetail (Long taskId, String assignPosition, ArrayList<TskStatusDetail> statusDetails) {  	
    	return Flux.fromIterable(statusDetails).flatMap(statusDetail -> {
    		statusDetail.setId(null);
    		statusDetail.setTaskId(taskId);
    		statusDetail.setAssignPosition(assignPosition);
    		return saveEntity(statusDetailRepo, statusDetail, auth).flatMapMany(savedStatusDetail -> {
    			return saveStatusDetailAttachFile(savedStatusDetail.getId(), statusDetail.getAttachFiles());
    		});
    	});
    	
    }
    
    
    private Flux<Object> saveStatusDetailAttachFile (Long statusDetailId, ArrayList<String> fileNames) {
    	var entity = new TskStatusAttachFile();
    	entity.setStatusDetailId(statusDetailId);
    	
    	return Flux.fromIterable(fileNames).flatMap(fileName -> {
    		entity.setFileName(fileName);
    		return saveEntity(statusAttachFileRepo, entity, auth);
    	});
    	
    }
    
    
    private Flux<Object> updateStatusDetail (ArrayList<TskStatusDetail> statusDetails) {  	
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
    			
    			return updateEntity(statusDetailRepo, found, auth).flatMap(updated -> {
    				
    				Mono<Void> deleteStatusAttachFile$ = Mono.empty();
    				if(statusDetail.getRemoveAttachFiles().size() > 0) {
    					deleteStatusAttachFile$  = statusAttachFileRepo.deleteByStatusDetailId(updated.getId(), statusDetail.getRemoveAttachFiles());
    		    	}
    				
    				var saveStatusAttachFile$ = Flux.empty();
    				if (statusDetail.getInsertAttachFiles().size() > 0) {
    					saveStatusAttachFile$ = saveStatusDetailAttachFile(updated.getId(), statusDetail.getInsertAttachFiles());
    				}
    				
    				return Flux.concat(deleteStatusAttachFile$, saveStatusAttachFile$).collectList();
    			});
    			
    		});
    		
    	});
    	
    }
}