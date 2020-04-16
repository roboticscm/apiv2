package vn.com.sky.sys.usersettings;

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
import vn.com.sky.security.AuthenticationManager;
import vn.com.sky.sys.model.UserSettings;
import vn.com.sky.util.MyServerResponse;

@Configuration
@AllArgsConstructor
public class UserSettingsREST extends GenericREST {
    private UserSettingsRepo mainRepo;
    private CustomUserSettingsRepo customRepo;
    private AuthenticationManager auth;

    @Bean
    public RouterFunction<?> userSettingsRoutes() {
        return route(GET(buildURL("user-settings", this::getUserSettings)), this::getUserSettings)
            .andRoute(GET(buildURL("user-settings", this::sysGetUserSettings)), this::sysGetUserSettings)
            .andRoute(POST(buildURL("user-settings", this::saveOrUpdate)), this::saveOrUpdate);
    }

    private Mono<ServerResponse> sysGetUserSettings(ServerRequest request) {
        // SYSTEM BLOCK CODE
        // PLEASE DO NOT EDIT
        if (request == null) {
            String methodName = new Object() {}.getClass().getEnclosingMethod().getName();
            return Mono.just(new MyServerResponse(methodName));
        }
        // END SYSTEM BLOCK CODE

        var companyIdStr = getParam(request, "companyId");
        Long companyId = null;

        try {
            companyId = Long.parseLong(companyIdStr);
        } catch (Exception e) {
            e.printStackTrace();
            return badRequest().bodyValue("SYS.MSG.INVILID_COMPANY_ID");
        }

        try {
            return customRepo
                .sysGetUserSettings(auth.getUserId(), companyId)
                .flatMap(item -> ok(item))
                .onErrorResume(e -> error(e));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private Mono<ServerResponse> getUserSettings(ServerRequest request) {
        // SYSTEM BLOCK CODE
        // PLEASE DO NOT EDIT
        if (request == null) {
            String methodName = new Object() {}.getClass().getEnclosingMethod().getName();
            return Mono.just(new MyServerResponse(methodName));
        }
        // END SYSTEM BLOCK CODE

        var menuPathStr = request.queryParam("menuPath").orElse(null);
        var controlId = request.queryParam("controlId").orElse(null);

        Long userId = auth.getUserId();

        System.out.println(menuPathStr);
        System.out.println(controlId);
        System.out.println(userId);

        if (menuPathStr == null) return badRequest().bodyValue("SYS.MSG.INVILID_MENU_PATH");

        if (controlId == null) return badRequest().bodyValue("SYS.MSG.INVILID_CONTROL_ID");

        try {
            return ServerResponse
                .ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(mainRepo.findByUserIdAndMenuPathAndControlId(userId, menuPathStr, controlId), UserSettings.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
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
            .bodyToMono(UserSettingsReq.class)
            .flatMap(
                req -> {
                    System.out.println(req);
                    return Flux
                        .fromIterable(req.getKeys())
                        .flatMap(
                            key -> {
                                return mainRepo
                                    .findByUserIdAndMenuPathAndControlIdAndKey(
                                        auth.getUserId(),
                                        req.getMenuPath(),
                                        req.getControlId(),
                                        key
                                    )
                                    .flatMap(
                                        foundUserSettings -> {
                                            // update

                                            foundUserSettings.setValue(req.getValue(key));
                                            return mainRepo.save(foundUserSettings);
                                        }
                                    )
                                    .switchIfEmpty(save(req, key, req.getValue(key))); // add new
                            }
                        )
                        .collectList()
                        .flatMap(mono -> ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).bodyValue("OK"));
                }
            );
    }

    private Mono<UserSettings> save(UserSettingsReq req, String key, String value) {
        var newUserSettings = new UserSettings();

        newUserSettings.setMenuPath(req.getMenuPath());
        newUserSettings.setControlId(req.getControlId());
        newUserSettings.setUserId(auth.getUserId());
        newUserSettings.setKey(key);
        newUserSettings.setValue(value);

        return mainRepo.save(newUserSettings);
    }
}
