package vn.com.sky.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.web.server.context.WebSessionServerSecurityContextRepository;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Created by IntelliJ IDEA.
 *
 * @Author: khai.lv (roboticscm2018@gmail.com)
 * Date: 3/19/2019
 * Time: 6:28 PM
 */

@Component
public class SecurityContextRepository
    extends WebSessionServerSecurityContextRepository /*implements ServerSecurityContextRepository*/{
    public static final String seperator = "||| ";
    public static final String screenSeperator = "!!!";

    @Autowired
    private AuthenticationManager authenticationManager;

    @Override
    public Mono<Void> save(ServerWebExchange swe, SecurityContext sc) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Mono<SecurityContext> load(ServerWebExchange swe) {
        ServerHttpRequest request = swe.getRequest();
        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        //        System.out.println(authHeader);
        if (authHeader != null && authHeader.contains(seperator)) {
            var index = authHeader.indexOf(seperator);
            var authToken = authHeader.substring(index + seperator.length());
            Authentication auth = new UsernamePasswordAuthenticationToken(authToken, authToken);
            return this.authenticationManager.authenticate(auth)
                .map(
                    authentication -> {
                        return new SecurityContextImpl(authentication);
                    }
                );
        } else {
            return Mono.empty();
        }
    }

    public static String getRequestUsername(ServerRequest request) {
        var authHeader = request.headers().asHttpHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        if (authHeader != null && authHeader.contains(seperator)) {
            var index = authHeader.indexOf(seperator);
            var username = authHeader.substring(0, index);
            return username;
        }

        return null;
    }

    public static String getRequestScreen(ServerRequest request) {
        var authHeader = request.headers().asHttpHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader != null && authHeader.contains(seperator)) {
            var index = authHeader.indexOf(screenSeperator);
            if (index >= 0) {
                return authHeader.substring(0, index);
            }
        }

        return null;
    }

    public static String getRequestFunction(ServerRequest request) {
        var authHeader = request.headers().asHttpHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        if (authHeader != null && authHeader.contains(seperator)) {
            var screenIndex = authHeader.indexOf(screenSeperator);
            var funcIndex = authHeader.indexOf(screenSeperator, screenIndex);

            if (screenIndex >= 0 && funcIndex >= 0) {
                return authHeader.substring(
                    screenIndex + screenSeperator.length(),
                    screenIndex + screenSeperator.length() + funcIndex
                );
            }
        }

        return null;
    }

    public static String getRequestPayload(ServerRequest request) {
        var authHeader = request.headers().asHttpHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        if (authHeader != null) {
            return getPayloadFromToken(authHeader);
        }

        return null;
    }

    public static String getRequestToken(ServerRequest request) {
        var authHeader = request.headers().asHttpHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        if (authHeader != null && authHeader.contains(seperator)) {
            var index = authHeader.indexOf(seperator);
            var username = authHeader.substring(index + seperator.length());
            return username;
        }

        return null;
    }

    public static String getPayloadFromToken(String token) {
        var firstPointIndex = token.indexOf(".");
        var secondPointIndex = token.indexOf(".", firstPointIndex + 1);
        return token.substring(firstPointIndex + 1, secondPointIndex);
    }
}
