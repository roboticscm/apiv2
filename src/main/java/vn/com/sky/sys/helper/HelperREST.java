package vn.com.sky.sys.helper;

import static org.springframework.web.reactive.function.server.RequestPredicates.DELETE;
import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.PUT;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;
import static org.springframework.web.reactive.function.server.ServerResponse.badRequest;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import lombok.AllArgsConstructor;
import reactor.core.publisher.Mono;
import vn.com.sky.Message;
import vn.com.sky.base.GenericREST;
import vn.com.sky.util.MyServerResponse;
import vn.com.sky.util.QueryUtil;
import vn.com.sky.util.SDate;

@Configuration
@AllArgsConstructor
public class HelperREST extends GenericREST {
    private CustomHelperRepo customRepo;

    @Bean
    public RouterFunction<?> helperRoutes() {
        return route(GET(buildURL("helper", this::isManager)), this::isManager);
           
    }

    private Mono<ServerResponse> isManager(ServerRequest request) {
        // SYSTEM BLOCK CODE
        // PLEASE DO NOT EDIT
        if (request == null) {
            String methodName = new Object() {}.getClass().getEnclosingMethod().getName();
            return Mono.just(new MyServerResponse(methodName));
        }
        // END SYSTEM BLOCK CODE

        Long managerId = null, staffId = null, depId = null;
		try {
			managerId = getLongParam(request, "managerId");
			staffId = getLongParam(request, "staffId");
			depId = getLongParam(request, "depId");
		} catch (Exception e1) {
			e1.printStackTrace();
		}
        
        var menuPath = getParam(request, "menuPath", null);
        
        if (managerId == null)
        	return error("managerId", "SYS.MSG.INVILID_MANAGER_ID");
        if (staffId == null)
        	return error("staffId", "SYS.MSG.INVILID_STAFF_ID");
        if (menuPath == null)
        	return error("menuPath", "SYS.MSG.INVILID_MENU_PATH");
        if (depId == null)
        	return error("depId", "SYS.MSG.INVILID_DEPARTMENT_ID");


        return customRepo
            .isManager(managerId, staffId, menuPath, depId)
            .flatMap(item -> ok(item, Boolean.class))
            .onErrorResume(e -> error(e));
    }
}
