package vn.com.sky.sys.menuhistory;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.POST;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import vn.com.sky.base.GenericREST;
import vn.com.sky.security.AuthenticationManager;
import vn.com.sky.sys.menu.MenuRepo;
import vn.com.sky.sys.model.MenuHistory;
import vn.com.sky.util.MyServerResponse;
import vn.com.sky.util.SDate;

@Configuration
@AllArgsConstructor
public class MenuHistoryREST extends GenericREST {
    private MenuHistoryRepo mainRepo;
    private AuthenticationManager auth;
    private MenuRepo menuRepo;

    @Bean
    public RouterFunction<?> menuHistoryRoutes() {
        return route(GET(buildURL("menu-history", this::test)), this::test)
            .andRoute(POST(buildURL("menu-history", this::saveOrUpdate)), this::saveOrUpdate);
    }

    private Mono<ServerResponse> test(ServerRequest request) {
        // SYSTEM BLOCK CODE
        // PLEASE DO NOT EDIT
        if (request == null) {
            String methodName = new Object() {}.getClass().getEnclosingMethod().getName();
            return Mono.just(new MyServerResponse(methodName));
        }
        // END SYSTEM BLOCK CODE
        return ok("");
    }

    private Mono<ServerResponse> saveOrUpdate(ServerRequest request) {
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
            .bodyToMono(MenuHistoryReq.class)
            .flatMap(
                req -> {
                    return mainRepo
                        .findByUserIdDepIdAndMenuPath(auth.getUserId(), req.getDepartmentId(), req.getMenuPath())
                        .flatMap(
                            foundMenuHistory -> {
                                foundMenuHistory.setLastAccess(SDate.now());
                                return super.updateEntity(mainRepo, foundMenuHistory, auth);
                            }
                        )
                        .switchIfEmpty(saveMenuHistory(req))
                        .flatMap(item -> ok(item, MenuHistory.class))
                        .onErrorResume(e -> error(e));
                }
            );
    }

    private Mono<MenuHistory> saveMenuHistory(MenuHistoryReq req) {
        return menuRepo
            .findByPath(req.getMenuPath())
            .flatMap(
                foundMenu -> {
                    var menuHistory = new MenuHistory();
                    menuHistory.setMenuId(foundMenu.getId());
                    menuHistory.setDepId(req.getDepartmentId());
                    menuHistory.setHumanId(auth.getUserId());
                    menuHistory.setLastAccess(SDate.now());
                    return super.saveEntity(mainRepo, menuHistory, auth);
                }
            );
    }
}
