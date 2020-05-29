package vn.com.sky.sys.searchutil;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.POST;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import lombok.AllArgsConstructor;
import reactor.core.publisher.Mono;
import vn.com.sky.base.GenericREST;
import vn.com.sky.sys.model.SearchFieldMenu;
import vn.com.sky.util.MyServerResponse;

@Configuration
@AllArgsConstructor
public class SearchUtilREST extends GenericREST {
    private CustomSearchUtilRepo customRepo;
    private SearchFieldRepo mainRepo;
    private SearchFieldMenuRepo fieldMenuRepo;

    @Bean
    public RouterFunction<?> searchUtilRoutes() {
        return route(GET(buildURL("search-util", this::findSearchFieldListByMenuPath)), this::findSearchFieldListByMenuPath)
            .andRoute(POST(buildURL("search-util", this::updateCounter)), this::updateCounter);
    }

    private Mono<ServerResponse> findSearchFieldListByMenuPath(ServerRequest request) {
        // SYSTEM BLOCK CODE
        // PLEASE DO NOT EDIT
        if (request == null) {
            String methodName = new Object() {}.getClass().getEnclosingMethod().getName();
            return Mono.just(new MyServerResponse(methodName));
        }
        // END SYSTEM BLOCK CODE

        var menuPath = getParam(request, "menuPath");
       
        if (menuPath == null) return error("menuPath", "SYS.MSG.INVILID_MENU_PATH");


        return customRepo
            .findSearchFieldListByMenuPath(menuPath)
            .flatMap(item -> ok(item))
            .onErrorResume(e -> error(e));
    }

    

    private Mono<ServerResponse> updateCounter(ServerRequest request) {
        // SYSTEM BLOCK CODE
        // PLEASE DO NOT EDIT
        if (request == null) {
            String methodName = new Object() {}.getClass().getEnclosingMethod().getName();
            return Mono.just(new MyServerResponse(methodName));
        }
        // END SYSTEM BLOCK CODE

        return request.bodyToMono(SearchFieldReq.class)
        	.flatMap(req -> {
        		return fieldMenuRepo.findByFieldAndMenuPath(req.getField(), req.getMenuPath()).flatMap(found -> {
        			found.setCounter(found.getCounter() != null ? found.getCounter() + 1 : 1);
        			return updateEntity(fieldMenuRepo, found, getUserId(request));
        		});
        	}).flatMap(item -> ok(item, SearchFieldMenu.class))
            .onErrorResume(e -> error(e));
    }
   
}
