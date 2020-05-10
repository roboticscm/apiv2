package vn.com.sky.sys.ownerorg;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.POST;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;
import static org.springframework.web.reactive.function.server.ServerResponse.badRequest;

import java.util.LinkedHashMap;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import io.netty.util.internal.StringUtil;
import lombok.AllArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import vn.com.sky.base.GenericREST;
import vn.com.sky.security.AuthenticationManager;
import vn.com.sky.sys.model.OwnerOrg;
import vn.com.sky.util.CustomRepoUtil;
import vn.com.sky.util.LinkedHashMapUtil;
import vn.com.sky.util.MyServerResponse;

@Configuration
@AllArgsConstructor
public class OwnerOrgREST extends GenericREST {
    private CustomOwnerOrgRepo customRepo;
    private AuthenticationManager auth;
    private OwnerOrgRepo mainRepo;
    private CustomRepoUtil utilRepo;

    @Bean
    public RouterFunction<?> ownerOrgRoutes() {
        return route(GET(buildURL("owner-org", this::sysGetOwnerOrgTree)), this::sysGetOwnerOrgTree)
            .andRoute(GET(buildURL("owner-org", this::sysGetCompanyList)), this::sysGetCompanyList)
            .andRoute(GET(buildURL("owner-org", this::sysGetOwnerOrgRoleTree)), this::sysGetOwnerOrgRoleTree)
            .andRoute(GET(buildURL("owner-org", this::sysGetOwnerOrgHumanTree)), this::sysGetOwnerOrgHumanTree)
            .andRoute(
                GET(buildURL("owner-org", this::sysGetAvailableDepartmentTreeForMenu)),
                this::sysGetAvailableDepartmentTreeForMenu
            )
            .andRoute(
                GET(buildURL("owner-org", this::sysGetDepartmentTreeByMenuId)),
                this::sysGetDepartmentTreeByMenuId
            )
            .andRoute(GET(buildURL("owner-org", this::sysGetHumanOrgTree)), this::sysGetHumanOrgTree)
            .andRoute(POST(buildURL("owner-org", this::saveOrUpdate)), this::saveOrUpdate)
            .andRoute(GET(buildURL("owner-org", this::sysGetAssignedHumanOrgTree)), this::sysGetAssignedHumanOrgTree)
            .andRoute(
                GET(buildURL("owner-org", this::sysGetRoledDepartmentListByUserId)),
                this::sysGetRoledDepartmentListByUserId
            );
    }
    
    private Mono<List<LinkedHashMap<String, String>>> validateForSave(OwnerOrg req) {
        var validateCode = utilRepo
            .isTextValueExisted("owner_org", "code", req.getCode())
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

        var validateName = mainRepo.isNameExisted(req.getName(), req.getParentId())
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
        return Flux.concat(validateCode, validateName).collectList();
    }
    
    private Mono<List<LinkedHashMap<String, String>>> validateForUpdate(OwnerOrg req) {
        var validateCode = utilRepo
            .isTextValueDuplicated("owner_org", "code", req.getCode(), req.getId())
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

        return Flux.concat(validateCode).collectList();
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
                .bodyToMono(OwnerOrg.class)
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
                                            return saveEntity(mainRepo, req, auth).flatMap(e -> ok(e, OwnerOrg.class));
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
                                            return updateEntity(mainRepo, req, auth).flatMap(e -> ok(e, OwnerOrg.class));
                                        }
                                    }
                                );
                        }
                    }
                );
    }
    
    private Mono<ServerResponse> sysGetCompanyList(ServerRequest request) {
        // SYSTEM BLOCK CODE
        // PLEASE DO NOT EDIT
        if (request == null) {
            String methodName = new Object() {}.getClass().getEnclosingMethod().getName();
            return Mono.just(new MyServerResponse(methodName));
        }
        // END SYSTEM BLOCK CODE

        try {
            return mainRepo
                .findAllCompanyList()
                .collectList()
                .flatMap(
                    item -> {
                        return ok(item, List.class);
                    }
                )
                .onErrorResume(e -> error(e));
        } catch (Exception e) {
            e.printStackTrace();
            return badRequest().bodyValue(e.getMessage());
        }
    }

    private Mono<ServerResponse> sysGetOwnerOrgTree(ServerRequest request) {
        // SYSTEM BLOCK CODE
        // PLEASE DO NOT EDIT
        if (request == null) {
            String methodName = new Object() {}.getClass().getEnclosingMethod().getName();
            return Mono.just(new MyServerResponse(methodName));
        }
        // END SYSTEM BLOCK CODE

        Long parentId = null;
		try {
			parentId = getLongParam(request, "parentId");
		} catch (Exception e1) {
			// TODO Auto-generated catch block
		}
        Boolean includeDeleted = false, includeDisabled = false;

        try {
            includeDeleted = super.getIncludeDeleted(request);
            includeDisabled = super.getIncludeDisabled(request);
        } catch (Exception e) {
            e.printStackTrace();
            return badRequest().bodyValue(e.getMessage());
        }

        try {
            return customRepo
                .sysGetOwnerOrgTree(parentId, includeDeleted, includeDisabled)
                .flatMap(item -> ok(item))
                .onErrorResume(e -> error(e));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private Mono<ServerResponse> sysGetOwnerOrgRoleTree(ServerRequest request) {
        // SYSTEM BLOCK CODE
        // PLEASE DO NOT EDIT
        if (request == null) {
            String methodName = new Object() {}.getClass().getEnclosingMethod().getName();
            return Mono.just(new MyServerResponse(methodName));
        }
        // END SYSTEM BLOCK CODE

        Boolean includeDeleted = false, includeDisabled = false;

        try {
            includeDeleted = super.getIncludeDeleted(request);
            includeDisabled = super.getIncludeDisabled(request);
        } catch (Exception e) {
            e.printStackTrace();
            return badRequest().bodyValue("SYS.MSG.INVILID_INCLUDE_DELETED");
        }

        try {
            return customRepo
                .sysGetOwnerOrgRoleTree(includeDeleted, includeDisabled)
                .flatMap(item -> ok(item))
                .onErrorResume(e -> error(e));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    
    private Mono<ServerResponse> sysGetOwnerOrgHumanTree(ServerRequest request) {
        // SYSTEM BLOCK CODE
        // PLEASE DO NOT EDIT
        if (request == null) {
            String methodName = new Object() {}.getClass().getEnclosingMethod().getName();
            return Mono.just(new MyServerResponse(methodName));
        }
        // END SYSTEM BLOCK CODE

        var filter = getParam(request, "filter");
        var excludeHumanIds = getParam(request, "excludeHumanIds");
        
        if (StringUtil.isNullOrEmpty(filter)) {
        	filter = null;
        }
        
        if (StringUtil.isNullOrEmpty(excludeHumanIds)) {
        	excludeHumanIds = null;
        }
        
        Boolean includeDeleted = false, includeDisabled = false;

        try {
            includeDeleted = super.getIncludeDeleted(request);
            includeDisabled = super.getIncludeDisabled(request);
        } catch (Exception e) {
            e.printStackTrace();
            return badRequest().bodyValue("SYS.MSG.INVILID_INCLUDE_DELETED");
        }

        try {
            return customRepo
                .sysGetOwnerOrgHumanTree(filter, excludeHumanIds, includeDeleted, includeDisabled)
                .flatMap(item -> ok(item))
                .onErrorResume(e -> error(e));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    

    private Mono<ServerResponse> sysGetRoledDepartmentListByUserId(ServerRequest request) {
        // SYSTEM BLOCK CODE
        // PLEASE DO NOT EDIT
        if (request == null) {
            String methodName = new Object() {}.getClass().getEnclosingMethod().getName();
            return Mono.just(new MyServerResponse(methodName));
        }
        // END SYSTEM BLOCK CODE
        Long userId = auth.getUserId();
        Boolean includeDeleted = false, includeDisabled = false;

        try {
            includeDeleted = super.getIncludeDeleted(request);
            includeDisabled = super.getIncludeDisabled(request);
        } catch (Exception e) {
            e.printStackTrace();
            return badRequest().bodyValue("SYS.MSG.INVILID_INCLUDE_DELETED");
        }

        try {
            return customRepo
                .sysGetRoledDepartmentListByUserId(userId, includeDeleted, includeDisabled)
                .flatMap(item -> ok(item))
                .onErrorResume(e -> error(e));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private Mono<ServerResponse> sysGetDepartmentTreeByMenuId(ServerRequest request) {
        // SYSTEM BLOCK CODE
        // PLEASE DO NOT EDIT
        if (request == null) {
            String methodName = new Object() {}.getClass().getEnclosingMethod().getName();
            return Mono.just(new MyServerResponse(methodName));
        }
        // END SYSTEM BLOCK CODE

        var menuIdStr = getParam(request, "menuId");

        Long menuId = null;

        try {
            if (menuIdStr != null && !"null".equals(menuIdStr)) menuId = Long.parseLong(menuIdStr);
            if (menuId == null) return error("menuId", "SYS.MSG.INVILID_MENU_ID");
        } catch (Exception e) {
            e.printStackTrace();
            return error("menuId", "SYS.MSG.INVILID_MENU_ID");
        }

        return customRepo.sysGetDepartmentTreeByMenuId(menuId).flatMap(item -> ok(item)).onErrorResume(e -> error(e));
    }

    private Mono<ServerResponse> sysGetAvailableDepartmentTreeForMenu(ServerRequest request) {
        // SYSTEM BLOCK CODE
        // PLEASE DO NOT EDIT
        if (request == null) {
            String methodName = new Object() {}.getClass().getEnclosingMethod().getName();
            return Mono.just(new MyServerResponse(methodName));
        }
        // END SYSTEM BLOCK CODE

        var menuIdStr = request.queryParam("menuId").orElse(null);

        Long menuId = null;

        try {
            if (menuIdStr != null && !"null".equals(menuIdStr)) menuId = Long.parseLong(menuIdStr);
        } catch (Exception e) {
            e.printStackTrace();
            return error("menuId", "SYS.MSG.INVILID_MENU_ID");
        }

        return customRepo
            .sysGetAvailableDepartmentTreeForMenu(menuId)
            .flatMap(item -> ok(item))
            .onErrorResume(e -> error(e));
    }

    private Mono<ServerResponse> sysGetHumanOrgTree(ServerRequest request) {
        // SYSTEM BLOCK CODE
        // PLEASE DO NOT EDIT
        if (request == null) {
            String methodName = new Object() {}.getClass().getEnclosingMethod().getName();
            return Mono.just(new MyServerResponse(methodName));
        }
        // END SYSTEM BLOCK CODE

        Long humanId;
        try {
            humanId = getLongParam(request, "humanId", null);
        } catch (Exception e1) {
            humanId = null;
        }

        return customRepo.sysGetHumanOrgTree(humanId).flatMap(item -> ok(item)).onErrorResume(e -> error(e));
    }

    private Mono<ServerResponse> sysGetAssignedHumanOrgTree(ServerRequest request) {
        // SYSTEM BLOCK CODE
        // PLEASE DO NOT EDIT
        if (request == null) {
            String methodName = new Object() {}.getClass().getEnclosingMethod().getName();
            return Mono.just(new MyServerResponse(methodName));
        }
        // END SYSTEM BLOCK CODE

        Long humanId;
        try {
            humanId = getLongParam(request, "humanId", null);
        } catch (Exception e1) {
            return error("humanId", "SYS.MSG.INVILID_HUMAN_ID");
        }

        return customRepo.sysGetAssignedHumanOrgTree(humanId).flatMap(item -> ok(item)).onErrorResume(e -> error(e));
    }
}
