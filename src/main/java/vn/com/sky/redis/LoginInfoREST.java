//package vn.com.sky.redis;
//
//import static org.springframework.web.reactive.function.server.RequestPredicates.DELETE;
//import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
//import static org.springframework.web.reactive.function.server.RouterFunctions.route;
//import static org.springframework.web.reactive.function.server.ServerResponse.ok;
//
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
//import org.springframework.data.redis.core.ReactiveRedisOperations;
//import org.springframework.http.MediaType;
//import org.springframework.web.reactive.function.server.RouterFunction;
//import org.springframework.web.reactive.function.server.ServerRequest;
//import org.springframework.web.reactive.function.server.ServerResponse;
//
//import lombok.AllArgsConstructor;
//import reactor.core.publisher.Mono;
//import vn.com.sky.security.SecurityContextRepository;
//
///**
// * Created by IntelliJ IDEA.
// *
// * @Author: khai.lv (roboticscm2018@gmail.com)
// * Date: 3/20/2019
// * Time: 7:11 AM
// */
//@Configuration
//@AllArgsConstructor
//public class LoginInfoREST {
//    private ReactiveRedisOperations<String, LoginInfo> loginInfoOps;
//    private final ReactiveRedisConnectionFactory factory;
//
//    @Bean
//    public RouterFunction<?> loginInfoRoutes(){
//        return route(DELETE("/api/system/redis/delete-all"), this::deleteAllHandler)
//                .andRoute(GET("/api/system/redis/get-user-info"), this::getUserInfoHandler);
//    }
//
//    private Mono<ServerResponse> getUserInfoHandler(ServerRequest request) {
//    	var payload = SecurityContextRepository.getRequestPayload(request);
//        return ok()
//            .contentType(MediaType.APPLICATION_JSON)
//            .body(loginInfoOps.keys(payload).flatMap(loginInfoOps.opsForValue()::get), LoginInfo.class);
//
//    }
//
//    private Mono<ServerResponse> deleteAllHandler(ServerRequest request) {
//        return ok()
//	        .contentType(MediaType.TEXT_PLAIN)
//	        .body(factory.getReactiveConnection().serverCommands().flushAll().thenReturn("Deleted"), String.class);
//
//    }
//}
//
