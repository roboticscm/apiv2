package vn.com.sky.sys.role;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.POST;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;
import static org.springframework.web.reactive.function.server.ServerResponse.badRequest;

import java.util.LinkedHashMap;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import lombok.AllArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import vn.com.sky.base.GenericREST;
import vn.com.sky.sys.model.Role;
import vn.com.sky.util.CustomRepoUtil;
import vn.com.sky.util.LinkedHashMapUtil;
import vn.com.sky.util.MyServerResponse;

@Configuration
@AllArgsConstructor
public class RoleREST extends GenericREST {
    @Autowired
    private CustomRoleRepo customRepo;

    @Autowired
    private CustomRepoUtil utilRepo;

    @Autowired
    private RoleRepo mainRepo;


    @Bean
    public RouterFunction<?> roleRoutes() {
        return route(GET(buildURL("role", this::sysGetRoleListByOrgId)), this::sysGetRoleListByOrgId)
            .andRoute(POST(buildURL("role", this::saveOrUpdate)), this::saveOrUpdate);
    }

    private Mono<ServerResponse> sysGetRoleListByOrgId(ServerRequest request) {
        // SYSTEM BLOCK CODE
        // PLEASE DO NOT EDIT
        if (request == null) {
            String methodName = new Object() {}.getClass().getEnclosingMethod().getName();
            return Mono.just(new MyServerResponse(methodName));
        }
        // END SYSTEM BLOCK CODE

        var orgIdStr = request.queryParam("orgId").orElse(null);

        Long orgId = null;
        Boolean includeDeleted = false, includeDisabled = false;

        try {
            if (orgIdStr != null && !"null".equals(orgIdStr)) orgId = Long.parseLong(orgIdStr);
        } catch (Exception e) {
            e.printStackTrace();
            return badRequest().bodyValue("SYS.MSG.INVILID_ORG_ID");
        }

        try {
            includeDeleted = super.getIncludeDeleted(request);
            includeDisabled = super.getIncludeDisabled(request);
        } catch (Exception e) {
            e.printStackTrace();
            return badRequest().bodyValue(e.getMessage());
        }

        try {
            return customRepo
                .sysGetRoleListByOrgId(orgId, includeDeleted, includeDisabled)
                .flatMap(item -> ok(item))
                .onErrorResume(e -> error(e));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private Mono<List<LinkedHashMap<String, String>>> validateForSave(Role roleReq) {
        var validateCode = utilRepo
            .isTextValueExisted("role", "code", roleReq.getCode())
            .flatMap(
                codeExisted -> {
                    if (codeExisted) {
                        var serverError = new LinkedHashMap<String, String>();
                        serverError.put("code", "SYS.MSG.CODE_EXISTED");
                        return Mono.just(serverError);
                    }
                    return Mono.empty();
                }
            );

        var validateName = utilRepo
            .isTextValueExisted("role", "name", roleReq.getName())
            .flatMap(
                codeExisted -> {
                    if (codeExisted) {
                        var serverError = new LinkedHashMap<String, String>();
                        serverError.put("name", "SYS.MSG.NAME_EXISTED");
                        return Mono.just(serverError);
                    }
                    return Mono.empty();
                }
            );

        return Flux.concat(validateCode, validateName).collectList();
    }

    private Mono<List<LinkedHashMap<String, String>>> validateForUpdate(Role roleReq) {
        var validateCode = utilRepo
            .isTextValueDuplicated("role", "code", roleReq.getCode(), roleReq.getId())
            .flatMap(
                codeExisted -> {
                    if (codeExisted) {
                        var serverError = new LinkedHashMap<String, String>();
                        serverError.put("code", "SYS.MSG.CODE_EXISTED");
                        return Mono.just(serverError);
                    }
                    return Mono.empty();
                }
            );

        var validateName = utilRepo
            .isTextValueDuplicated("role", "name", roleReq.getName(), roleReq.getId())
            .flatMap(
                codeExisted -> {
                    if (codeExisted) {
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
            .bodyToMono(Role.class)
            .flatMap(
                roleReq -> {
                    // client validation
                    var clientErrors = validate(roleReq);
                    if (clientErrors != null) return clientErrors;

                    if (roleReq.getId() == null) { // save
                        return validateForSave(roleReq)
                            .flatMap(
                                errs -> {
                                    if (errs.size() > 0) {
                                        return error(LinkedHashMapUtil.fromArrayList(errs));
                                    } else {
                                        return saveEntity(mainRepo, roleReq, getUserId(request)).flatMap(e -> ok(e, Role.class));
                                    }
                                }
                            );
                    } else { // update
                        return validateForUpdate(roleReq)
                            .flatMap(
                                errs -> {
                                    if (errs.size() > 0) {
                                        return error(LinkedHashMapUtil.fromArrayList(errs));
                                    } else {
                                        return updateEntity(mainRepo, roleReq, getUserId(request)).flatMap(e -> ok(e, Role.class));
                                    }
                                }
                            );
                    }
                }
            );
    }
}
