package vn.com.sky.sys.notification;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.POST;
import static org.springframework.web.reactive.function.server.RequestPredicates.PUT;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

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
import vn.com.sky.sys.model.PartNotification;
import vn.com.sky.util.MyServerResponse;

@Configuration
@AllArgsConstructor
public class NotificationREST extends GenericREST {
    private CustomNotificationRepo customRepo;
    private NotificationRepo mainRepo;

    @Bean
    public RouterFunction<?> notificationRoutes() {
        return route(GET(buildURL("notification", this::findNotifications)), this::findNotifications)
        	.andRoute(PUT(buildURL("notification", this::update)), this::update)
            .andRoute(POST(buildURL("notification", this::save)), this::save);
    }

    private Mono<ServerResponse> findNotifications(ServerRequest request) {
        // SYSTEM BLOCK CODE
        // PLEASE DO NOT EDIT
        if (request == null) {
            String methodName = new Object() {}.getClass().getEnclosingMethod().getName();
            return Mono.just(new MyServerResponse(methodName));
        }
        // END SYSTEM BLOCK CODE
        var textSearch = getParam(request, "textSearch");
        var type = getParam(request, "type");
        return customRepo
                .findNotifications(getUserId(request), type, textSearch)
                .flatMap(json -> {
                	return ok(json);
                	});
    }

  
    private Mono<ServerResponse> save(ServerRequest request) {
        // SYSTEM BLOCK CODE
        // PLEASE DO NOT EDIT
        if (request == null) {
            String methodName = new Object() {}.getClass().getEnclosingMethod().getName();
            return Mono.just(new MyServerResponse(methodName));
        }
        // END SYSTEM BLOCK CODE

        return request
            .bodyToMono(PartNotification.class)
            .flatMap(req -> {
            	return Flux.fromIterable(req.getToHumanListIds())
            		.flatMap(toHumanId -> {
            			req.setToHumanId(toHumanId);
            			return saveEntity(mainRepo, req, getUserId(request));
            		}).collectList().flatMap(res -> ok(res, List.class));
            	
            });
    }
    
    
    private Mono<ServerResponse> update(ServerRequest request) {
        // SYSTEM BLOCK CODE
        // PLEASE DO NOT EDIT
        if (request == null) {
            String methodName = new Object() {}.getClass().getEnclosingMethod().getName();
            return Mono.just(new MyServerResponse(methodName));
        }
        // END SYSTEM BLOCK CODE

        return request
            .bodyToMono(PartNotification.class)
            .flatMap(req -> {
            	System.out.println(req);
            	System.out.println(req.getId());
            	return mainRepo.findById(req.getId())
            			.flatMap(found -> {
            				if(req.getIsRead() != null) {
            					found.setIsRead(req.getIsRead());
            				}
            				
            				if(req.getIsFinished() != null) {
            					found.setIsFinished(req.getIsFinished());
            				}
            				return updateEntity(mainRepo, found, getUserId(request)).flatMap(res -> ok(res, PartNotification.class));
            			});
            	
            });
    }
}
