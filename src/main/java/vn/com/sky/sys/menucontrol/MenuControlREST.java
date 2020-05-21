package vn.com.sky.sys.menucontrol;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.POST;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import lombok.AllArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import vn.com.sky.base.GenericREST;
import vn.com.sky.sys.menu.MenuRepo;
import vn.com.sky.sys.model.MenuControl;
import vn.com.sky.util.MyServerResponse;

@Configuration
@AllArgsConstructor
public class MenuControlREST extends GenericREST {
    private MenuControlRepo mainRepo;
    private CustomMenuControlRepo customRepo;
    private MenuRepo menuRepo;

    @Bean
    public RouterFunction<?> menuControlRoutes() {
        return route(
                GET(buildURL("menu-control", this::sysGetControlListByMenuPath)),
                this::sysGetControlListByMenuPath
            )
            .andRoute(POST(buildURL("menu-control", this::saveOrUpdateOrDelete)), this::saveOrUpdateOrDelete);
    }

    private Mono<ServerResponse> sysGetControlListByMenuPath(ServerRequest request) {
        // SYSTEM BLOCK CODE
        // PLEASE DO NOT EDIT
        if (request == null) {
            String methodName = new Object() {}.getClass().getEnclosingMethod().getName();
            return Mono.just(new MyServerResponse(methodName));
        }
        // END SYSTEM BLOCK CODE

        var menuPath = request.queryParam("menuPath").orElse(null);

        if (menuPath == null) return error("menuPath", "SYS.MSG.INVILID_MENU_PATH");

        return customRepo.sysGetControlListByMenuPath(menuPath).flatMap(item -> ok(item)).onErrorResume(e -> error(e));
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
            .bodyToMono(MenuControlReq.class)
            .flatMap(
                menuControlReq -> {
                    return menuRepo
                        .findByPath(menuControlReq.getMenuPath())
                        .flatMap(
                            foundMenu -> {
                                return Flux
                                    .fromIterable(menuControlReq.getMenuControls())
                                    .flatMap(
                                        menuControl -> {
                                            return mainRepo
                                                .findByMenuIdAndControlId(foundMenu.getId(), menuControl.getControlId())
                                                .flatMap(
                                                    foundMenuControl -> {
                                                        if (!menuControl.getChecked()) {
                                                            return mainRepo.delete(foundMenuControl);
                                                        }
                                                        return Mono.empty();
                                                    }
                                                )
                                                .switchIfEmpty(saveMenuControl(getUserId(request), menuControl, foundMenu.getId()));
                                        }
                                    )
                                    .collectList();
                            }
                        );
                }
            )
            .flatMap(res -> ok(res, MenuControl.class));
    }

    private Mono<Void> saveMenuControl(Long userId, MenuControl menuControl, Long menuId) {
        if (menuControl.getChecked()) {
            menuControl.setMenuId(menuId);
            return super.saveEntity(mainRepo, menuControl, userId).flatMap(ret -> Mono.empty());
        } else {
            return Mono.empty();
        }
    }
}
