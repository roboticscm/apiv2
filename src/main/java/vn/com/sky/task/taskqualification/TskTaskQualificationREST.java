package vn.com.sky.task.taskqualification;

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
import vn.com.sky.task.model.TskTaskQualification;
import vn.com.sky.util.LinkedHashMapUtil;
import vn.com.sky.util.MyServerResponse;

@Configuration
@AllArgsConstructor
public class TskTaskQualificationREST extends GenericREST {
    private TskTaskQualificationRepo mainRepo;

    @Bean
    public RouterFunction<?> tskTaskQualificationRoutes() {
        return route(POST(buildURL(Constants.API_TASK_PREFIX, "task-qualification", this::saveOrUpdate)), this::saveOrUpdate);
    }
    
    private Mono<List<LinkedHashMap<String, String>>> validateForSave(TskTaskQualification req) {
      
    	var validateCode = utilRepo
                .isTextValueExisted(getTableName(req), "code", req.getCode())
                .flatMap(
                    existed -> {
                        if (existed) {
                            var serverError = new LinkedHashMap<String, String>();
                            serverError.put("code", "SYS.MSG.CODE_EXISTED");
                            return Mono.just(serverError);
                        }
                        return Mono.empty();
                    }
                );
    	
        var validateName = utilRepo
            .isTextValueExisted(getTableName(req), "name", req.getName())
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

        return Flux.concat(validateCode, validateName).collectList();
    }
    
    private Mono<List<LinkedHashMap<String, String>>> validateForUpdate(TskTaskQualification req) {
    	var validateCode = utilRepo
                .isTextValueDuplicated(getTableName(req), "code", req.getCode(), req.getId())
                .flatMap(
                    existed -> {
                        if (existed) {
                            var serverError = new LinkedHashMap<String, String>();
                            serverError.put("code", "SYS.MSG.CODE_EXISTED");
                            return Mono.just(serverError);
                        }
                        return Mono.empty();
                    }
                );
    	
        
        var validateName = utilRepo
            .isTextValueDuplicated(getTableName(req), "name", req.getName(), req.getId())
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

        return Flux.concat(validateCode, validateName).collectList();
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
                .bodyToMono(TskTaskQualification.class)
                .flatMap(
                    req -> {
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
                                            return saveEntity(mainRepo, req, getUserId(request)).flatMap(e -> {
                                            	return ok(e, TskTaskQualification.class);
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
                                            return updateEntity(mainRepo, req, getUserId(request)).flatMap(e -> ok(e, TskTaskQualification.class));
                                        }
                                    }
                                );
                        }
                    }
                );
    }
}
