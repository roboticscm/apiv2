//package vn.com.sky.sys.globalparam;
//
//import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
//import static org.springframework.web.reactive.function.server.RequestPredicates.POST;
//import static org.springframework.web.reactive.function.server.RouterFunctions.route;
//import static org.springframework.web.reactive.function.server.ServerResponse.badRequest;
//
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.http.MediaType;
//import org.springframework.web.reactive.function.server.RouterFunction;
//import org.springframework.web.reactive.function.server.ServerRequest;
//import org.springframework.web.reactive.function.server.ServerResponse;
//
//import lombok.AllArgsConstructor;
//import reactor.core.publisher.Flux;
//import reactor.core.publisher.Mono;
//import vn.com.sky.base.GenericREST;
//import vn.com.sky.security.AuthenticationManager;
//import vn.com.sky.sys.model.UserSettings;
//import vn.com.sky.util.MyServerResponse;
//
//@Configuration
//@AllArgsConstructor
//public class GlobalParamREST extends GenericREST {
//	private GlobalParamRepo mainRepo;
//
//	@Bean
//	public RouterFunction<?> userSettingsRoutes() {
//		return route(GET(buildURL("user-settings", this::getUserSettings)), this::getUserSettings)
//			.andRoute(POST(buildURL("user-settings", this::saveOrUpdate)), this::saveOrUpdate);
//	}
//
//
//
//	private Mono<ServerResponse> getUserSettings(ServerRequest request) {
//		// SYSTEM BLOCK CODE
//		// PLEASE DO NOT EDIT
//		if (request == null) {
//			String methodName = new Object() {
//			}.getClass().getEnclosingMethod().getName();
//			return Mono.just(new MyServerResponse(methodName));
//		}
//		// END SYSTEM BLOCK CODE
//
//		var menuPathStr = request.queryParam("menuPath").orElse(null);
//		var	controlId = request.queryParam("controlId").orElse(null);
//
//		Long userId = auth.getUserId();
//
//
//		if (menuPathStr == null)
//			return badRequest().bodyValue("SYS.MSG.INVILID_MENU_PATH");
//
//		if (controlId == null)
//			return badRequest().bodyValue("SYS.MSG.INVILID_CONTROL_ID");
//
//
//		try {
//			return ServerResponse.ok().contentType(MediaType.APPLICATION_JSON)
//				.body(mainRepo.findByUserIdAndMenuPathAndControlId(userId, menuPathStr, controlId), UserSettings.class);
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//		return null;
//	}
//
//	private Mono<ServerResponse> saveOrUpdate(ServerRequest request) {
//		// SYSTEM BLOCK CODE
//		// PLEASE DO NOT EDIT
//		if (request == null) {
//			String methodName = new Object() {
//			}.getClass().getEnclosingMethod().getName();
//			return Mono.just(new MyServerResponse(methodName));
//		}
//		// END SYSTEM BLOCK CODE
//
//
//
//		return request.bodyToMono(UserSettingsReq.class).flatMap(req -> {
//			return Flux.fromIterable(req.getKeys()).flatMap(key -> {
//				return mainRepo.findByUserIdAndMenuPathAndControlIdAndKey(auth.getUserId(), req.getMenuPath(),  req.getControlId(), key).flatMap(foundUserSettings -> {
//					// update
//
//					foundUserSettings.setValue(req.getValue(key));
//					return mainRepo.save(foundUserSettings);
//				}).switchIfEmpty(save(req, key, req.getValue(key))); // add new
//			}).collectList().flatMap(mono -> ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).bodyValue("OK"));
//
//		});
//	}
//
//	private Mono<UserSettings> save(UserSettingsReq req, String key, String value) {
//		var newUserSettings = new UserSettings();
//
//		newUserSettings.setMenuPath(req.getMenuPath());
//		newUserSettings.setControlId(req.getControlId());
//		newUserSettings.setUserId(auth.getUserId());
//		newUserSettings.setKey(key);
//		newUserSettings.setValue(value);
//
//		return mainRepo.save(newUserSettings);
//
//	}
//
//}
