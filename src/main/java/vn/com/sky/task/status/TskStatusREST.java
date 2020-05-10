package vn.com.sky.task.status;

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
import vn.com.sky.task.model.TskPriority;
import vn.com.sky.task.model.TskStatus;
import vn.com.sky.util.LinkedHashMapUtil;
import vn.com.sky.util.MyServerResponse;

@Configuration
@AllArgsConstructor
public class TskStatusREST extends GenericREST {
    private TskStatusRepo mainRepo;
    private AuthenticationManager auth;

    @Bean
    public RouterFunction<?> tskStatusRoutes() {
        return route(POST(buildURL(Constants.API_TASK_PREFIX, "status", this::saveOrUpdate)), this::saveOrUpdate);
    }
    
    private Mono<List<LinkedHashMap<String, String>>> validateForSave(TskStatus req) {
      
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
    
    private Mono<List<LinkedHashMap<String, String>>> validateForUpdate(TskStatus req) {
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
                .bodyToMono(TskStatus.class)
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
                                            return saveEntity(mainRepo, req, auth).flatMap(e -> {
                                            	return ok(e, TskStatus.class);
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
                                            return updateEntity(mainRepo, req, auth).flatMap(e -> ok(e, TskStatus.class));
                                        }
                                    }
                                );
                        }
                    }
                );
    }
}
