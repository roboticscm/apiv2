package vn.com.sky.sys.assignmentrole;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.POST;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;
import static org.springframework.web.reactive.function.server.ServerResponse.badRequest;

import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import vn.com.sky.base.GenericREST;
import vn.com.sky.base.RepoUtil;
import vn.com.sky.security.AuthenticationManager;
import vn.com.sky.sys.model.AssignmentRole;
import vn.com.sky.sys.model.HumanOrOrg;
import vn.com.sky.sys.model.Role;
import vn.com.sky.util.MyServerResponse;

@Configuration
@AllArgsConstructor
public class AssignmentRoleREST extends GenericREST {
    private AssignmentRoleRepo mainRepo;
    private CustomAssignmentRoleRepo customRepo;
    private AuthenticationManager auth;

    @Bean
    public RouterFunction<?> assignmentRoleRoutes() {
        return route(
                GET(buildURL("assignment-role", this::sysGetAllAssignmentRoleUserList)),
                this::sysGetAllAssignmentRoleUserList
            )
            .andRoute(GET(buildURL("assignment-role", this::sysGetRoleListOfUser)), this::sysGetRoleListOfUser)
            .andRoute(GET(buildURL("assignment-role", this::sysGetRoleListOfUsers)), this::sysGetRoleListOfUsers)
            .andRoute(POST(buildURL("assignment-role", this::saveOrUpdateOrDelete)), this::saveOrUpdateOrDelete);
    }

    private Mono<ServerResponse> sysGetAllAssignmentRoleUserList(ServerRequest request) {
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
                .sysGetAllAssignmentRoleUserList(includeDeleted, includeDisabled)
                .flatMap(item -> ok(item))
                .onErrorResume(e -> error(e));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private Mono<ServerResponse> sysGetRoleListOfUser(ServerRequest request) {
        // SYSTEM BLOCK CODE
        // PLEASE DO NOT EDIT
        if (request == null) {
            String methodName = new Object() {}.getClass().getEnclosingMethod().getName();
            return Mono.just(new MyServerResponse(methodName));
        }
        // END SYSTEM BLOCK CODE

        var userIdStr = request.queryParam("userId").orElse(null);

        Long userId = null;
        Boolean includeDeleted = false, includeDisabled = false;

        try {
            if (userIdStr != null && !"null".equals(userIdStr)) userId = Long.parseLong(userIdStr);
            if (userId == null) return badRequest().bodyValue("SYS.MSG.INVILID_USER_ID");
        } catch (Exception e) {
            e.printStackTrace();
            return badRequest().bodyValue("SYS.MSG.INVILID_USER_ID");
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
                .sysGetRoleListOfUser(userId, includeDeleted, includeDisabled)
                .flatMap(item -> ok(item))
                .onErrorResume(e -> error(e));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private Mono<ServerResponse> sysGetRoleListOfUsers(ServerRequest request) {
        // SYSTEM BLOCK CODE
        // PLEASE DO NOT EDIT
        if (request == null) {
            String methodName = new Object() {}.getClass().getEnclosingMethod().getName();
            return Mono.just(new MyServerResponse(methodName));
        }
        // END SYSTEM BLOCK CODE

        var userIds = getParam(request, "userIds");

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
                .sysGetRoleListOfUsers(userIds, includeDeleted, includeDisabled)
                .flatMap(item -> ok(item))
                .onErrorResume(e -> error(e));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private Mono<ServerResponse> saveOrUpdateOrDelete(ServerRequest request) {
        // SYSTEM BLOCK CODE
        // PLEASE DO NOT EDIT
        if (request == null) {
            String methodName = new Object() {}.getClass().getEnclosingMethod().getName();
            return Mono.just(new MyServerResponse(methodName));
        }
        // END SYSTEM BLOCK CODE

        //		return request.body(BodyExtractors.toFormData()).flatMap(item->{
        //			System.out.println(item);
        //			return ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).bodyValue("OK");
        //		});

        return request
            .bodyToMono(AssignmentRoleReq.class)
            .flatMap(
                req -> {
                    return Flux
                        .fromIterable(req.getUsers())
                        .flatMap(
                            user -> {
                                return Flux
                                    .fromIterable(req.getRoles())
                                    .flatMap(
                                        role -> {
                                            return mainRepo
                                                .findByUserIdAndRoleId(user.getId(), role.getId())
                                                .flatMap(
                                                    ar -> {
                                                        // update
                                                        if (role.getChecked()) { // update
                                                            ar.setDisabled(role.getDisabled());
                                                            return RepoUtil.update(mainRepo, ar, auth);
                                                        } else { // delete
                                                            return RepoUtil.softDelete(mainRepo, ar, auth);
                                                        }
                                                    }
                                                )
                                                .switchIfEmpty(save(request, user, role)); // add new
                                        }
                                    );
                            }
                        )
                        .collectList()
                        .flatMap(mono -> ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).bodyValue("OK"));
                }
            );
    }

    private Mono<AssignmentRole> save(ServerRequest request, HumanOrOrg user, Role role) {
        System.out.println(role);
        if (role.getChecked()) {
            var newAssignmentRole = new AssignmentRole();

            newAssignmentRole.setUserId(user.getId());
            newAssignmentRole.setRoleId(role.getId());
            newAssignmentRole.setDisabled(role.getDisabled());

            return RepoUtil.save(mainRepo, newAssignmentRole, auth);
        } else {
            return Mono.empty();
        }
    }
}
