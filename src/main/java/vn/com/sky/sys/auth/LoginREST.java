package vn.com.sky.sys.auth;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.POST;
import static org.springframework.web.reactive.function.server.RequestPredicates.PUT;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;
import static org.springframework.web.reactive.function.server.ServerResponse.permanentRedirect;

import java.net.URI;
import java.net.URISyntaxException;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import vn.com.sky.base.GenericREST;
import vn.com.sky.sys.menu.MenuReq;
import vn.com.sky.sys.model.AuthToken;
import vn.com.sky.util.MyServerResponse;

@Configuration
@AllArgsConstructor
public class LoginREST extends GenericREST {
    @Autowired
    private LoginService loginService;

    private AuthTokenRepo authTokenRepo;

    @Bean
    public RouterFunction<?> loginRoutes() {
        return route(POST("/api/sys/auth/login"), this::loginHandler)
            .andRoute(POST(buildURL("auth", this::loginWithoutGenToken)), this::loginWithoutGenToken)
            .andRoute(POST(buildURL("auth", this::getQrcode)), this::getQrcode)
            .andRoute(POST(buildURL("auth", this::updateAuthToken)), this::updateAuthToken)
            .andRoute(GET("/api/sys/auth/reset-password"), request -> ServerResponse.ok().render("login/resetpw"))
            .andRoute(
                GET("/document"),
                request -> {
                    try {
                        return permanentRedirect(new URI("index.html")).build();
                    } catch (URISyntaxException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                        return error(e);
                    }
                }
            )
            .andRoute(GET("/"), request -> ServerResponse.ok().render("login/login"));
    }

    private Mono<ServerResponse> loginWithoutGenToken(ServerRequest request) {
        // SYSTEM BLOCK CODE
        // PLEASE DO NOT EDIT
        if (request == null) {
            String methodName = new Object() {}.getClass().getEnclosingMethod().getName();
            return Mono.just(new MyServerResponse(methodName));
        }
        // END SYSTEM BLOCK CODE
        try {
            return loginService
                .loginWithoutGenToken(request)
                .flatMap(item -> ok(item, SimpleAuthResponse.class))
                .onErrorResume(e -> error(e));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private Mono<ServerResponse> getQrcode(ServerRequest request) {
        // SYSTEM BLOCK CODE
        // PLEASE DO NOT EDIT
        if (request == null) {
            String methodName = new Object() {}.getClass().getEnclosingMethod().getName();
            return Mono.just(new MyServerResponse(methodName));
        }
        // END SYSTEM BLOCK CODE

        return authTokenRepo
            .save(new AuthToken())
            .map(savedAuthToken -> savedAuthToken.getId())
            .flatMap(token -> ok(token, Long.class));
    }

    private Mono<ServerResponse> updateAuthToken(ServerRequest request) {
        // SYSTEM BLOCK CODE
        // PLEASE DO NOT EDIT
        if (request == null) {
            String methodName = new Object() {}.getClass().getEnclosingMethod().getName();
            return Mono.just(new MyServerResponse(methodName));
        }
        // END SYSTEM BLOCK CODE

        return request
            .bodyToMono(AuthTokenReq.class)
            .flatMap(
                req -> {
                    return authTokenRepo
                        .findById(req.getId())
                        .flatMap(
                            foundAuthToken -> {
                                foundAuthToken.setToken(req.getToken());
                                foundAuthToken.setUserId(req.getUserId().toString());
                                foundAuthToken.setLastLocaleLanguage(req.getLastLocaleLanguage());
                                foundAuthToken.setCompanyId(req.getCompanyId().toString());
                                System.out.println(foundAuthToken);
                                return authTokenRepo.save(foundAuthToken).flatMap(res -> ok(res, AuthToken.class));
                            }
                        );
                }
            );
    }

    private Mono<ServerResponse> loginHandler(ServerRequest request) {
        request
            .remoteAddress()
            .ifPresent(
                action -> {
                    System.out.println("IP" + action);
                }
            );
        try {
            return loginService
                .login(request)
                .flatMap(item -> ok(item, AuthResponse.class))
                .onErrorResume(e -> error(e));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
