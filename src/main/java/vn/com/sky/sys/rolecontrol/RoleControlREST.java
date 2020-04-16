package vn.com.sky.sys.rolecontrol;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;
import static org.springframework.web.reactive.function.server.ServerResponse.badRequest;

import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import vn.com.sky.base.GenericREST;
import vn.com.sky.security.AuthenticationManager;
import vn.com.sky.util.MyServerResponse;

@Configuration
@AllArgsConstructor
public class RoleControlREST extends GenericREST {
    private CustomRoleControlRepo customRepo;
    private AuthenticationManager auth;

    @Bean
    public RouterFunction<?> roleControlRoutes() {
        return route(
            GET(buildURL("role-control", this::sysGetControlListByDepIdAndUserIdAndMenuPath)),
            this::sysGetControlListByDepIdAndUserIdAndMenuPath
        );
    }

    private Mono<ServerResponse> sysGetControlListByDepIdAndUserIdAndMenuPath(ServerRequest request) {
        // SYSTEM BLOCK CODE
        // PLEASE DO NOT EDIT
        if (request == null) {
            String methodName = new Object() {}.getClass().getEnclosingMethod().getName();
            return Mono.just(new MyServerResponse(methodName));
        }
        // END SYSTEM BLOCK CODE

        var depIdStr = request.queryParam("depId").orElse(null);
        var menuPath = request.queryParam("menuPath").orElse(null);
        Long depId = null;

        try {
            if (depIdStr != null && !"null".equals(depIdStr)) depId = Long.parseLong(depIdStr);
        } catch (Exception e) {
            e.printStackTrace();
            return badRequest().bodyValue("SYS.MSG.INVILID_DEPARTMENT_ID");
        }

        if (menuPath == null) return badRequest().bodyValue("SYS.MSG.INVILID_MENU_PATH");

        try {
            return customRepo
                .sysGetControlListByDepIdAndUserIdAndMenuPath(depId, auth.getUserId(), menuPath)
                .flatMap(item -> ok(item))
                .onErrorResume(e -> error(e));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
