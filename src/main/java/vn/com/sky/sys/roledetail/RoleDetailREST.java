package vn.com.sky.sys.roledetail;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.POST;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;
import static org.springframework.web.reactive.function.server.ServerResponse.badRequest;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import lombok.AllArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import vn.com.sky.base.GenericREST;
import vn.com.sky.sys.menucontrol.MenuControlRepo;
import vn.com.sky.sys.menuorg.MenuOrgRepo;
import vn.com.sky.sys.model.RoleControl;
import vn.com.sky.sys.model.RoleDetail;
import vn.com.sky.sys.rolecontrol.RoleControlRepo;
import vn.com.sky.util.MyServerResponse;

@Configuration
@AllArgsConstructor
public class RoleDetailREST extends GenericREST {
    private RoleDetailRepo mainRepo;
    private MenuOrgRepo menuOrgRepo;
    private CustomRoleDetailRepo customRepo;
    private RoleControlRepo roleControlRepo;
    private MenuControlRepo menuControlRepo;

    @Bean
    public RouterFunction<?> roleDetailRoutes() {
        return route(GET(buildURL("role-detail", this::sysGetMenuRoleControlList)), this::sysGetMenuRoleControlList)
            .andRoute(POST(buildURL("role-detail", this::saveOrUpdateOrDelete)), this::saveOrUpdateOrDelete);
    }

    private Mono<ServerResponse> sysGetMenuRoleControlList(ServerRequest request) {
        // SYSTEM BLOCK CODE
        // PLEASE DO NOT EDIT
        if (request == null) {
            String methodName = new Object() {}.getClass().getEnclosingMethod().getName();
            return Mono.just(new MyServerResponse(methodName));
        }
        // END SYSTEM BLOCK CODE

        var ownerOrgIdStr = request.queryParam("ownerOrgId").orElse(null);
        var roleIdStr = request.queryParam("roleId").orElse(null);

        Long roleId = null, ownerOrgId = null;
        Boolean includeDeleted = false, includeDisabled = false;

        try {
            if (roleIdStr != null && !"null".equals(roleIdStr)) roleId = Long.parseLong(roleIdStr);

            if (roleId == null) return badRequest().bodyValue("SYS.MSG.INVILID_ROLE_ID");
        } catch (Exception e) {
            e.printStackTrace();
            return badRequest().bodyValue("SYS.MSG.INVILID_ROLE_ID");
        }

        try {
            if (ownerOrgIdStr != null && !"null".equals(ownerOrgIdStr)) ownerOrgId = Long.parseLong(ownerOrgIdStr);

            if (ownerOrgId == null) return badRequest().bodyValue("SYS.MSG.INVILID_OWNER_ORG_ID");
        } catch (Exception e) {
            e.printStackTrace();
            return badRequest().bodyValue("SYS.MSG.INVILID_OWNER_ORG_ID");
        }

        
        try {
            includeDeleted = getIncludeDeleted(request);
            includeDisabled = getIncludeDisabled(request);
        } catch (Exception e) {
            e.printStackTrace();
            return badRequest().bodyValue(e.getMessage());
        }

        try {
            return customRepo
                .sysGetMenuRoleControlList(ownerOrgId, roleId, includeDeleted, includeDisabled)
                .flatMap(item -> ok(item))
                .onErrorResume(e -> error(e));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private Mono<ServerResponse> saveOrUpdateOrDelete(ServerRequest request) {
        if (request == null) {
            String methodName = new Object() {}.getClass().getEnclosingMethod().getName();
            return Mono.just(new MyServerResponse(methodName));
        }

        return request
            .bodyToMono(RoleDetailReq.class)
            .flatMap(
                item -> {
                    return Flux
                        .fromIterable(item.getRoleDetailWithControls())
                        .filter(
                            roleDetailWithControl ->
                                roleDetailWithControl.getMenuName() != null &&
                                roleDetailWithControl.getMenuName().trim().length() > 0
                        )
                        .flatMap(
                            roleDetailWithControl -> {
                                return mainRepo
                                    .findByRoleIdAndDepartmentIdAndMenuId(
                                        item.getRoleId(),
                                        roleDetailWithControl.getDepartmentId(),
                                        roleDetailWithControl.getMenuId()
                                    )
                                    .flatMap(
                                        foundRoleDetail -> {
                                            // update role_detail
                                            if (roleDetailWithControl.getChecked()) {
                                                foundRoleDetail.setIsPrivate(roleDetailWithControl.getIsPrivate());
                                                foundRoleDetail.setApprove(roleDetailWithControl.getApprove());
                                                foundRoleDetail.setDataLevel(roleDetailWithControl.getDataLevel());
                                                return 
                                                    updateEntity(mainRepo, foundRoleDetail, getUserId(request))
                                                    .flatMap(
                                                        updatedRoleDetail -> {
                                                            return Flux
                                                                .fromIterable(item.getRoleDetailWithControls())
                                                                .filter(
                                                                    roleControl ->
                                                                        roleControl.getDepartmentId().equals(roleDetailWithControl.getDepartmentId())
                                                                         &&
                                                                        roleControl.getMenuId().equals(roleDetailWithControl.getMenuId())
                                                                        
                                                                )
                                                                .flatMap(
                                                                    roleControl -> {
                                                                        return roleControlRepo
                                                                            .findByRoleDetailIdAndMenuIdAndControlId(
                                                                                foundRoleDetail.getId(),
                                                                                roleControl.getMenuId(),
                                                                                roleControl.getControlId()
                                                                            )
                                                                            .flatMap(
                                                                                foundRoleControl -> {
                                                                                    // update role control
                                                                                    foundRoleControl.setRenderControl(
                                                                                        roleControl.getRenderControl()
                                                                                    );
                                                                                    foundRoleControl.setDisableControl(
                                                                                        roleControl.getDisableControl()
                                                                                    );
                                                                                    foundRoleControl.setConfirm(
                                                                                        roleControl.getConfirm()
                                                                                    );
                                                                                    foundRoleControl.setRequirePassword(
                                                                                        roleControl.getRequirePassword()
                                                                                    );
                                                                                    return updateEntity(
                                                                                        roleControlRepo,
                                                                                        foundRoleControl,
                                                                                        getUserId(request)
                                                                                    );
                                                                                }
                                                                            )
                                                                            .switchIfEmpty(
                                                                                saveRoleControl(
                                                                                    request,
                                                                                    foundRoleDetail.getId(),
                                                                                    roleControl
                                                                                )
                                                                            ); // save role control
                                                                    }
                                                                )
                                                                .collectList();
                                                        }
                                                    );
                                            } else { // delete
                                                return roleControlRepo
                                                    .deleteByRoleDetailId(foundRoleDetail.getId())
                                                    .then(mainRepo.delete(foundRoleDetail));
                                            }
                                        }
                                    )
                                    .switchIfEmpty(
                                        saveRoleDetail(request, item.getRoleId(), roleDetailWithControl, item)
                                    ); // add new
                            }
                        )
                        .collectList()
                        .flatMap(mono -> ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).bodyValue("OK"));
                }
            );
    }

    private Mono<List<RoleControl>> saveRoleDetail(
        ServerRequest request,
        Long roleId,
        RoleDetailWithControl roleDetailWithControl,
        RoleDetailReq roleDetailReq
    ) {

        return Mono.defer(
            () -> {
                if (roleDetailWithControl.getChecked()) {
                    var newRoleDetail = new RoleDetail();

                    newRoleDetail.setRoleId(roleId);
                    newRoleDetail.setIsPrivate(roleDetailWithControl.getIsPrivate());
                    newRoleDetail.setApprove(roleDetailWithControl.getApprove());
                    newRoleDetail.setDataLevel(roleDetailWithControl.getDataLevel());

                    return menuOrgRepo
                        .findByMenuIdAndOrgId(
                            roleDetailWithControl.getMenuId(),
                            roleDetailWithControl.getDepartmentId()
                        )
                        .flatMap(
                            menuOrg -> {
                                newRoleDetail.setMenuOrgId(menuOrg.getId());
                                return 
                                    saveEntity(mainRepo, newRoleDetail, getUserId(request))
                                    .flatMap(
                                        savedRoleDetail -> {
                                            return Flux
                                                .fromIterable(roleDetailReq.getRoleDetailWithControls())
                                                .filter(
                                                    roleControl -> {
                                                        return roleControl.getDepartmentId().equals(roleDetailWithControl.getDepartmentId()) 
                                                         &&
                                                        roleControl.getMenuId().equals(roleDetailWithControl.getMenuId()) ;
                                                    }
                                                )
                                                .flatMap(
                                                    roleControl -> {
                                                        return roleControlRepo
                                                            .findByRoleDetailIdAndMenuIdAndControlId(
                                                                savedRoleDetail.getId(),
                                                                roleControl.getMenuId(),
                                                                roleControl.getControlId()
                                                            )
                                                            .flatMap(
                                                                foundRoleControl -> {
                                                                    // update role control
                                                                    foundRoleControl.setRenderControl(
                                                                        roleControl.getRenderControl()
                                                                    );
                                                                    foundRoleControl.setDisableControl(
                                                                        roleControl.getDisableControl()
                                                                    );
                                                                    foundRoleControl.setConfirm(
                                                                        roleControl.getConfirm()
                                                                    );
                                                                    foundRoleControl.setRequirePassword(
                                                                        roleControl.getRequirePassword()
                                                                    );
                                                                    return updateEntity(
                                                                        roleControlRepo,
                                                                        foundRoleControl,
                                                                        getUserId(request)
                                                                    );
                                                                }
                                                            )
                                                            .switchIfEmpty(
                                                                saveRoleControl(
                                                                    request,
                                                                    savedRoleDetail.getId(),
                                                                    roleControl
                                                                )
                                                            ); // save role control
                                                    }
                                                )
                                                .collectList();
                                        }
                                    );
                            }
                        );
                } else {
                    return Mono.<List<RoleControl>>empty();
                }
            }
        );
    }

    private Mono<RoleControl> saveRoleControl(
        ServerRequest request,
        Long roleDetailId,
        RoleDetailWithControl roleDetailWithControl
    ) {
        return Mono.defer(
            () -> {
                var newRoleControl = new RoleControl();

                newRoleControl.setRoleDetailId(roleDetailId);
                newRoleControl.setRenderControl(roleDetailWithControl.getRenderControl());
                newRoleControl.setDisableControl(roleDetailWithControl.getDisableControl());
                newRoleControl.setConfirm(roleDetailWithControl.getConfirm());
                newRoleControl.setRequirePassword(roleDetailWithControl.getRequirePassword());

                return menuControlRepo
                    .findByMenuIdAndControlId(roleDetailWithControl.getMenuId(), roleDetailWithControl.getControlId())
                    .flatMap(
                        foundRoleControl -> {
                            newRoleControl.setMenuControlId(foundRoleControl.getId());
                            return saveEntity(roleControlRepo, newRoleControl, getUserId(request));
                        }
                    );
            }
        );
    }
}
