package vn.com.sky.sys.language;

import static org.springframework.web.reactive.function.server.RequestPredicates.POST;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

import java.util.LinkedHashMap;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import lombok.AllArgsConstructor;
import reactor.core.publisher.Mono;
import vn.com.sky.base.GenericREST;
import vn.com.sky.sys.model.Language;
import vn.com.sky.util.MyServerResponse;

@Configuration
@AllArgsConstructor
public class LanguageREST extends GenericREST {
    private LanguageRepo mainRepo;

    @Bean
    public RouterFunction<?> languageRoutes() {
        return route(POST(buildURL("language", this::saveOrUpdate)), this::saveOrUpdate);
    }

    private Mono<ServerResponse> saveOrUpdate(ServerRequest request) {
        // SYSTEM BLOCK CODE
        // PLEASE DO NOT EDIT
        if (request == null) {
            String methodName = new Object() {}.getClass().getEnclosingMethod().getName();
            return Mono.just(new MyServerResponse(methodName));
        }
        // END SYSTEM BLOCK CODE

        //		return request.body(BodyExtractors.toFormData()).flatMap(item->{
        //			System.out.println(item);
        //			return ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).bodyValue("OK");
        //		});

        return request
            .bodyToMono(Language.class)
            .flatMap(
                langReq -> {
                    var clientErrors = validate(langReq);
                    if (clientErrors != null) return clientErrors;

                    var serverError = new LinkedHashMap<String, String>();

                    if (langReq.getId() == null) { // save
                        return mainRepo
                            .isNameExisted(langReq.getName())
                            .flatMap(
                                nameExisted -> {
                                    if (nameExisted) {
                                        serverError.put("name", "SYS.MSG.NAME_EXISTED");
                                    }
                                    return mainRepo
                                        .isLocaleExisted(langReq.getLocale())
                                        .flatMap(
                                            localeExisted -> {
                                                if (localeExisted) {
                                                    serverError.put("locale", "SYS.MSG.LOCALE_EXISTED");
                                                }

                                                if (serverError.size() > 0) {
                                                    return error(serverError);
                                                } else {
                                                    return saveEntity(mainRepo, langReq, getUserId(request))
                                                        .flatMap(res -> ok(res, Language.class));
                                                }
                                            }
                                        );
                                }
                            );
                    } else { // update
                        return mainRepo
                            .findById(langReq.getId())
                            .flatMap(
                                foundLang -> {
                                    return mainRepo
                                        .isNameDuplicated(langReq.getName(), langReq.getId())
                                        .flatMap(
                                            nameExisted -> {
                                                if (nameExisted) {
                                                    serverError.put("name", "SYS.MSG.NAME_EXISTED");
                                                }
                                                return mainRepo
                                                    .isLocaleDuplicated(langReq.getLocale(), langReq.getId())
                                                    .flatMap(
                                                        localeExisted -> {
                                                            if (localeExisted) {
                                                                serverError.put("locale", "SYS.MSG.LOCALE_EXISTED");
                                                            }

                                                            if (serverError.size() > 0) {
                                                                return error(serverError);
                                                            } else {
                                                                return updateEntity(mainRepo, langReq, getUserId(request))
                                                                    .flatMap(res -> ok(res, Language.class));
                                                            }
                                                        }
                                                    );
                                            }
                                        );
                                }
                            );
                    }
                }
            );
    }
}
