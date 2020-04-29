package vn.com.sky.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import reactor.core.publisher.Mono;

/**
 * Created by IntelliJ IDEA.
 *
 * @Author: khai.lv (roboticscm2018@gmail.com)
 * Date: 3/19/2019
 * Time: 6:30 PM
 */

@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
public class WebSecurityConfig {
    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private SecurityContextRepository securityContextRepository;

    @Bean
    public SecurityWebFilterChain securitygWebFilterChain(ServerHttpSecurity http) {
        return http
            .exceptionHandling()
            .authenticationEntryPoint(
                (swe, e) -> {
                    return Mono.fromRunnable(
                        () -> {
                            swe.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                        }
                    );
                }
            )
            .accessDeniedHandler(
                (swe, e) -> {
                    return Mono.fromRunnable(
                        () -> {
                            swe.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                        }
                    );
                }
            )
            .and()
            .csrf()
            .disable()
            .formLogin()
            .disable()
            .httpBasic()
            .disable()
            .authenticationManager(authenticationManager)
            .securityContextRepository(securityContextRepository)
            .authorizeExchange()
            .pathMatchers(HttpMethod.OPTIONS)
            .permitAll()
            .pathMatchers(
                "/",
                "/document",
                "/index.html",
                "/js/main.js",
                "/js/index.js",
                "/js/qrcode.min.js",
                "/login",
                "/login/login",
                "/api/sys/news/get-list",
                "/api/sys/table-util/get-one-by-id",
                "/api/sys/auth/get-qrcode",
                "/api/sys/auth/reset-password",
                "/api/sys/auth/login",
                "/api/sys/auth/login-without-gen-token",
                "/images/**",
                "/favicon.ico",
                "/api/sys/locale-resource/sys-get-locale-resource-list-by-company-id-and-locale"
            )
            .permitAll()
            .anyExchange()
            .authenticated()
            .and()
            .build();
    }
}
