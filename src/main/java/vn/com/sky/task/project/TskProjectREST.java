package vn.com.sky.task.project;

import static org.springframework.web.reactive.function.server.RequestPredicates.POST;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

import java.util.LinkedHashMap;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import lombok.AllArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import vn.com.sky.Constants;
import vn.com.sky.base.GenericREST;
import vn.com.sky.security.AuthenticationManager;
import vn.com.sky.task.model.TskProject;
import vn.com.sky.util.LinkedHashMapUtil;
import vn.com.sky.util.MyServerResponse;

@Configuration
@AllArgsConstructor
public class TskProjectREST extends GenericREST {
    private TskProjectRepo mainRepo;
    private AuthenticationManager auth;

    @Bean
    public RouterFunction<?> tskProjectRoutes() {
        return route(POST(buildURL(Constants.API_TASK_PREFIX, "project", this::saveOrUpdate)), this::saveOrUpdate);
    }
    
    private Mono<List<LinkedHashMap<String, String>>> validateForSave(TskProject projectReq) {
      
        var validateName = utilRepo
            .isTextValueExisted("tsk_project", "name", projectReq.getName())
            .flatMap(
                nameExisted -> {
                    if (nameExisted) {
                        var serverError = new LinkedHashMap<String, String>();
                        serverError.put("name", "SYS.MSG.NAME_EXISTED");
                        return Mono.just(serverError);
                    }
                    return Mono.empty();
                }
            );

        return Flux.concat(validateName).collectList();
    }
    
    private Mono<List<LinkedHashMap<String, String>>> validateForUpdate(TskProject projectReq) {
        
        var validateName = utilRepo
            .isTextValueDuplicated("tsk_project", "name", projectReq.getName(), projectReq.getId())
            .flatMap(
                nameExisted -> {
                    if (nameExisted) {
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
                .bodyToMono(TskProject.class)
                .flatMap(
                    projectReq -> {
                        // client validation
                        var clientErrors = validate(projectReq);
                        if (clientErrors != null) return clientErrors;

                        if (projectReq.getId() == null) { // save
                            return validateForSave(projectReq)
                                .flatMap(
                                    errs -> {
                                        if (errs.size() > 0) {
                                            return error(LinkedHashMapUtil.fromArrayList(errs));
                                        } else {
                                            return saveEntity(mainRepo, projectReq, auth).flatMap(e -> {
                                            	return ok(e, TskProject.class);
                                            });
                                        }
                                    }
                                );
                        } else { // update
                            return validateForUpdate(projectReq)
                                .flatMap(
                                    errs -> {
                                        if (errs.size() > 0) {
                                            return error(LinkedHashMapUtil.fromArrayList(errs));
                                        } else {
                                            return updateEntity(mainRepo, projectReq, auth).flatMap(e -> ok(e, TskProject.class));
                                        }
                                    }
                                );
                        }
                    }
                );
    }
}
