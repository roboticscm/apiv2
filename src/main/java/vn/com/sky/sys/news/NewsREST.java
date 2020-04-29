package vn.com.sky.sys.news;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.POST;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.BodyExtractors;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import lombok.AllArgsConstructor;
import reactor.core.publisher.Mono;
import vn.com.sky.base.GenericREST;
import vn.com.sky.sys.model.News;
import vn.com.sky.util.MyServerResponse;
import vn.com.sky.util.SDate;

@Configuration
@AllArgsConstructor
public class NewsREST extends GenericREST {
	private NewsRepo mainRepo;

	@Bean
	public RouterFunction<?> newsRoutes() {
		return route(POST(buildURL("news", this::saveOrUpdate)), this::saveOrUpdate)
				.andRoute(GET(buildURL("news", this::getList)), this::getList);
	}

	private Mono<ServerResponse> saveOrUpdate(ServerRequest request) {
		// SYSTEM BLOCK CODE
		// PLEASE DO NOT EDIT
		if (request == null) {
			String methodName = new Object() {
			}.getClass().getEnclosingMethod().getName();
			return Mono.just(new MyServerResponse(methodName));
		}
		// END SYSTEM BLOCK CODE

		return request.body(BodyExtractors.toFormData()).flatMap(item -> {
			System.out.println(item);
			return ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).bodyValue("OK");
		});

	}
	
	
	private Mono<ServerResponse> getList(ServerRequest request) {
        // SYSTEM BLOCK CODE
        // PLEASE DO NOT EDIT
        if (request == null) {
            String methodName = new Object() {}.getClass().getEnclosingMethod().getName();
            return Mono.just(new MyServerResponse(methodName));
        }
        // END SYSTEM BLOCK CODE

 
        try {
            return ServerResponse
                .ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(mainRepo.findTop(SDate.now()), News.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
