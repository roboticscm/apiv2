package vn.com.sky.sys.menu;

import static org.springframework.web.reactive.function.server.RequestPredicates.DELETE;
import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.POST;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;
import static org.springframework.web.reactive.function.server.ServerResponse.badRequest;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
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
import vn.com.sky.base.GenericREST;
import vn.com.sky.sys.menuorg.MenuOrgRepo;
import vn.com.sky.sys.model.Menu;
import vn.com.sky.sys.model.MenuOrg;
import vn.com.sky.util.CustomRepoUtil;
import vn.com.sky.util.MyServerResponse;

@Configuration
@AllArgsConstructor
public class MenuREST extends GenericREST {
    private MenuRepo mainRepo;
    private CustomMenuRepo customRepo;
    private MenuOrgRepo menuOrgRepo;
    private CustomRepoUtil utilRepo;

    
    @Bean
    public RouterFunction<?> menuRoutes() {
        return route(
                GET(buildURL("menu", this::sysGetFirstRoledMenuPathByUserIdAndDepId)),
                this::sysGetFirstRoledMenuPathByUserIdAndDepId
            )
            .andRoute(
                GET(buildURL("menu", this::sysGetRoledMenuPathListByUserId)),
                this::sysGetRoledMenuPathListByUserId
            )
            .andRoute(GET(buildURL("menu", this::sysGetAllMenuList)), this::sysGetAllMenuList)
            .andRoute(GET(buildURL("menu", this::sysGetMenuById)), this::sysGetMenuById)
            .andRoute(GET(buildURL("menu", this::sysGetMenuByPath)), this::sysGetMenuByPath)
            .andRoute(
                GET(buildURL("menu", this::sysGetRoledMenuListByUserIdAndDepId)),
                this::sysGetRoledMenuListByUserIdAndDepId
            )
            .andRoute(POST(buildURL("menu", this::saveOrUpdate)), this::saveOrUpdate)
            .andRoute(DELETE(buildURL("menu", this::deleteMany)), this::deleteMany);
    }

    private Mono<ServerResponse> sysGetMenuById(ServerRequest request) {
        // SYSTEM BLOCK CODE
        // PLEASE DO NOT EDIT
        if (request == null) {
            String methodName = new Object() {}.getClass().getEnclosingMethod().getName();
            return Mono.just(new MyServerResponse(methodName));
        }
        // END SYSTEM BLOCK CODE

        //		var menuIdStr = request.queryParam("menuId").orElse(null);
        var menuIdStr = URLDecoder.decode(request.queryParam("menuId").orElse(null), StandardCharsets.UTF_8);
        Long menuId = null;

        try {
            if (menuIdStr != null && !"null".equals(menuIdStr)) menuId = Long.parseLong(menuIdStr);
            if (menuId == null) return error("menuId", "SYS.MSG.INVILID_MENU_ID");
        } catch (Exception e) {
            e.printStackTrace();
            return error("menuId", "SYS.MSG.INVILID_MENU_ID");
        }

        try {
            return mainRepo
                .findById(menuId)
                .flatMap(
                    item -> {
                        return ok(item, Menu.class);
                    }
                )
                .onErrorResume(e -> error(e));
        } catch (Exception e) {
            e.printStackTrace();
            return badRequest().bodyValue(e.getMessage());
        }
    }

    private Mono<ServerResponse> sysGetMenuByPath(ServerRequest request) {
        // SYSTEM BLOCK CODE
        // PLEASE DO NOT EDIT
        if (request == null) {
            String methodName = new Object() {}.getClass().getEnclosingMethod().getName();
            return Mono.just(new MyServerResponse(methodName));
        }
        // END SYSTEM BLOCK CODE

        var menuPath = getParam(request, "menuPath");

        if (menuPath == null) return error("menuPATH", "SYS.MSG.INVILID_MENU_PATH");

        try {
            return mainRepo
                .findByPath(menuPath)
                .flatMap(
                    item -> {
                        return ok(item, Menu.class);
                    }
                )
                .onErrorResume(e -> error(e));
        } catch (Exception e) {
            e.printStackTrace();
            return badRequest().bodyValue(e.getMessage());
        }
    }

    private Mono<ServerResponse> sysGetFirstRoledMenuPathByUserIdAndDepId(ServerRequest request) {
        // SYSTEM BLOCK CODE
        // PLEASE DO NOT EDIT
        if (request == null) {
            String methodName = new Object() {}.getClass().getEnclosingMethod().getName();
            return Mono.just(new MyServerResponse(methodName));
        }
        // END SYSTEM BLOCK CODE

        var depIdStr = request.queryParam("depId").orElse(null);

        Long userId = getUserId(request), depId = null;
        Boolean includeDeleted = false, includeDisabled = false;

        try {
            if (depIdStr != null && !"null".equals(depIdStr)) depId = Long.parseLong(depIdStr);
            if (depId == null) return error("depId", "SYS.MSG.INVILID_DEPARTMENT_ID");
        } catch (Exception e) {
            e.printStackTrace();
            return error("depId", "SYS.MSG.INVILID_DEPARTMENT_ID");
        }

        try {
            includeDeleted = super.getIncludeDeleted(request);
            includeDisabled = super.getIncludeDisabled(request);
        } catch (Exception e) {
            e.printStackTrace();
            return badRequest().bodyValue(e.getMessage());
        }

        return customRepo
            .sysGetFirstRoledMenuPathByUserIdAndDepId(userId, depId, includeDeleted, includeDisabled)
            .flatMap(item -> ok(item))
            .onErrorResume(e -> error(e));
    }

    private Mono<ServerResponse> sysGetAllMenuList(ServerRequest request) {
        // SYSTEM BLOCK CODE
        // PLEASE DO NOT EDIT
        if (request == null) {
            String methodName = new Object() {}.getClass().getEnclosingMethod().getName();
            return Mono.just(new MyServerResponse(methodName));
        }
        // END SYSTEM BLOCK CODE

        Boolean sortByCreatedDate = false;
        String sortByCreatedDateStr = request.queryParam("sortByCreatedDate").orElse("false");

        try {
            if (sortByCreatedDateStr != null && !"null".equals(sortByCreatedDateStr)) sortByCreatedDate =
                Boolean.parseBoolean(sortByCreatedDateStr);
        } catch (Exception e) {
            e.printStackTrace();
            return error("depId", "SYS.MSG.INVILID_SORT");
        }

        return customRepo.sysGetAllMenuList(sortByCreatedDate).flatMap(item -> ok(item)).onErrorResume(e -> error(e));
    }

    private Mono<ServerResponse> sysGetRoledMenuPathListByUserId(ServerRequest request) {
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

        return customRepo
            .sysGetRoledMenuPathListByUserId(getUserId(request), includeDeleted, includeDisabled)
            .flatMap(item -> ok(item))
            .onErrorResume(e -> error(e));
    }

    private Mono<ServerResponse> sysGetRoledMenuListByUserIdAndDepId(ServerRequest request) {
        // SYSTEM BLOCK CODE
        // PLEASE DO NOT EDIT
        if (request == null) {
            String methodName = new Object() {}.getClass().getEnclosingMethod().getName();
            return Mono.just(new MyServerResponse(methodName));
        }
        // END SYSTEM BLOCK CODE

        var depIdStr = request.queryParam("depId").orElse(null);

        Long userId = getUserId(request), depId = null;
        Boolean includeDeleted = false, includeDisabled = false;

        try {
            if (depIdStr != null && !"null".equals(depIdStr)) depId = Long.parseLong(depIdStr);
        } catch (Exception e) {
            e.printStackTrace();
            return error("depId", "SYS.MSG.INVILID_DEPARTMENT_ID");
        }

        try {
            includeDeleted = super.getIncludeDeleted(request);
            includeDisabled = super.getIncludeDisabled(request);
        } catch (Exception e) {
            e.printStackTrace();
            return badRequest().bodyValue(e.getMessage());
        }

        return customRepo
            .sysGetRoledMenuListByUserIdAndDepId(userId, depId, includeDeleted, includeDisabled)
            .flatMap(item -> ok(item))
            .onErrorResume(e -> error(e));
    }

    private Mono<ServerResponse> save(ServerRequest request, LinkedHashMap<String, String> serverError, MenuReq menuReq) {
        return utilRepo
            .isTextValueExisted("menu", "name", menuReq.getName())
            .flatMap(
                nameExisted -> {
                    if (nameExisted) {
                        serverError.put("name", "SYS.MSG.MENU_NAME_EXISTED");
                    }
                    return utilRepo
                        .isTextValueExisted("menu", "path", menuReq.getPath())
                        .flatMap(
                            pathExisted -> {
                                if (pathExisted) {
                                    serverError.put("path", "SYS.MSG.MENU_PATH_EXISTED");
                                }

                                if (serverError.size() > 0) {
                                    return error(serverError);
                                } else {
                                    return saveEntity(mainRepo, menuReq, getUserId(request))
                                        .flatMap(
                                            savedMenu -> {
                                                return saveManyMenuOrg(getUserId(request), savedMenu.getId(), menuReq.getInsertDepIds())
                                                    .flatMap(item -> ok(item, Menu.class))
                                                    .then(
                                                        deleteManyMenuOrg(savedMenu.getId(), menuReq.getDeleteDepIds())
                                                            .flatMap(res -> ok(res, List.class))
                                                    );
                                            }
                                        );
                                }
                            }
                        );
                }
            );
    }

    private Mono<ServerResponse> update(ServerRequest request, LinkedHashMap<String, String> serverError, MenuReq menuReq) {
        return utilRepo
            .isTextValueDuplicated("menu", "name", menuReq.getName(), menuReq.getId())
            .flatMap(
                nameExisted -> {
                    if (nameExisted) {
                        serverError.put("name", "SYS.MSG.MENU_NAME_EXISTED");
                    }

                    return utilRepo
                        .isTextValueDuplicated("menu", "path", menuReq.getPath(), menuReq.getId())
                        .flatMap(
                            pathExisted -> {
                                if (pathExisted) {
                                    serverError.put("path", "SYS.MSG.MENU_PATH_EXISTED");
                                }

                                if (serverError.size() > 0) {
                                    return error(serverError);
                                } else {
                                    return updateEntity(mainRepo, menuReq, getUserId(request))
                                        .flatMap(
                                            updatedMenu -> {
                                                return saveManyMenuOrg(getUserId(request), updatedMenu.getId(), menuReq.getInsertDepIds())
                                                    .flatMap(item -> ok(item, Menu.class))
                                                    .then(
                                                        deleteManyMenuOrg(
                                                                updatedMenu.getId(),
                                                                menuReq.getDeleteDepIds()
                                                            )
                                                            .flatMap(res -> ok(res, List.class))
                                                    );
                                            }
                                        );
                                }
                            }
                        );
                }
            );
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
            .bodyToMono(MenuReq.class)
            .flatMap(
                menuReq -> {
                    var clientErrors = validate(menuReq);
                    if (clientErrors != null) return clientErrors;

                    var serverError = new LinkedHashMap<String, String>();

                    if (menuReq.getId() == null) { // save
                        return save(request, serverError, menuReq);
                    } else { // update
                        return update(request, serverError, menuReq);
                    }
                }
            );
    }

    private Mono<List<Void>> deleteManyMenuOrg(Long menuId, ArrayList<Long> depIds) {
        if (depIds == null) {
            return Mono.empty();
        } else {
            return Flux
                .fromIterable(depIds)
                .flatMap(
                    depId -> {
                        return menuOrgRepo.deleteByMenuIdAndOrgId(menuId, depId);
                    }
                )
                .collectList();
        }
    }

    private Mono<List<MenuOrg>> saveManyMenuOrg(Long userId, Long menuId, ArrayList<Long> depIds) {
        if (depIds == null) {
            return Mono.empty();
        } else {
            return Flux
                .fromIterable(depIds)
                .flatMap(
                    depId -> {
                        return menuOrgRepo
                            .findByMenuIdAndOrgId(menuId, depId)
                            .switchIfEmpty(doSaveMenuOrg(userId, menuId, depId));
                    }
                )
                .collectList();
        }
    }

    private Mono<MenuOrg> doSaveMenuOrg(Long userId, Long menuId, Long depId) {
        var menuOrg = new MenuOrg();
        menuOrg.setOrgId(depId);
        menuOrg.setMenuId(menuId);
        return saveEntity(menuOrgRepo, menuOrg, userId);
    }

    private Mono<ServerResponse> deleteMany(ServerRequest request) {
        // SYSTEM BLOCK CODE
        // PLEASE DO NOT EDIT
        if (request == null) {
            String methodName = new Object() {}.getClass().getEnclosingMethod().getName();
            return Mono.just(new MyServerResponse(methodName));
        }
        // END SYSTEM BLOCK CODE

        var isPermanentlyDeletedStr = request.queryParam("isPermanentlyDeleted").orElse("false");
        Boolean isPermanentlyDeleted = false;

        try {
            isPermanentlyDeleted = Boolean.parseBoolean(isPermanentlyDeletedStr);
        } catch (Exception e) {
            e.printStackTrace();
            return error("isPermanentlyDeleted", "SYS.MSG.INVILID_PERMANEMTLY_DELETED");
        }

        List<Long> ids = null;
        try {
            ids = getIdsFromQueryParam(request);
        } catch (Exception e) {
            e.printStackTrace();
            return error("menuId", "SYS.MSG.INVILID_MENU_ID");
        }

        if (isPermanentlyDeleted) {
            return mainRepo.deleteByIds(ids).flatMap(res -> ok("deleted"));
        } else {
            return Flux
                .fromIterable(ids)
                .flatMap(
                    id -> {
                        return mainRepo
                            .findById(id)
                            .flatMap(
                                foundObj -> {
                                    return super.softDeleteEntity(mainRepo, foundObj, getUserId(request));
                                }
                            );
                    }
                )
                .collectList()
                .flatMap(res -> ok(res, List.class));
        }
    }
}
