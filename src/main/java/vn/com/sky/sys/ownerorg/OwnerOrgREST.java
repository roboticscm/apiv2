package vn.com.sky.sys.ownerorg;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;
import static org.springframework.web.reactive.function.server.ServerResponse.badRequest;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import vn.com.sky.Message;
import vn.com.sky.base.GenericREST;
import vn.com.sky.security.AuthenticationManager;
import vn.com.sky.sys.model.Menu;
import vn.com.sky.util.MyServerResponse;

@Configuration
@AllArgsConstructor
public class OwnerOrgREST extends GenericREST {
    private CustomOwnerOrgRepo customRepo;
    private AuthenticationManager auth;
    private OwnerOrgRepo mainRepo;

    @Bean
    public RouterFunction<?> ownerOrgRoutes() {
        return route(GET(buildURL("owner-org", this::sysGetOwnerOrgTree)), this::sysGetOwnerOrgTree)
            .andRoute(GET(buildURL("owner-org", this::sysGetCompanyList)), this::sysGetCompanyList)
            .andRoute(GET(buildURL("owner-org", this::sysGetOwnerOrgRoleTree)), this::sysGetOwnerOrgRoleTree)
            .andRoute(
                GET(buildURL("owner-org", this::sysGetAvailableDepartmentTreeForMenu)),
                this::sysGetAvailableDepartmentTreeForMenu
            )
            .andRoute(
                GET(buildURL("owner-org", this::sysGetDepartmentTreeByMenuId)),
                this::sysGetDepartmentTreeByMenuId
            )
            .andRoute(GET(buildURL("owner-org", this::sysGetHumanOrgTree)), this::sysGetHumanOrgTree)
            .andRoute(GET(buildURL("owner-org", this::sysGetAssignedHumanOrgTree)), this::sysGetAssignedHumanOrgTree)
            .andRoute(
                GET(buildURL("owner-org", this::sysGetRoledDepartmentListByUserId)),
                this::sysGetRoledDepartmentListByUserId
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
                .sysGetOwnerOrgTree(includeDeleted, includeDisabled)
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
