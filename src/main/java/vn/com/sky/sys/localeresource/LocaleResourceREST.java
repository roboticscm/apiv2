package vn.com.sky.sys.localeresource;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.POST;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;
import static org.springframework.web.reactive.function.server.ServerResponse.badRequest;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import lombok.AllArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import vn.com.sky.Message;
import vn.com.sky.base.GenericREST;
import vn.com.sky.security.AuthenticationManager;
import vn.com.sky.sys.model.LocaleResource;
import vn.com.sky.sys.ownerorg.OwnerOrgRepo;
import vn.com.sky.util.MyServerResponse;

@Configuration
@AllArgsConstructor
public class LocaleResourceREST extends GenericREST {
    private CustomLocaleResourceRepo customRepo;
    private LocaleResourceRepo mainRepo;
    private AuthenticationManager auth;
    private OwnerOrgRepo ownerOrgRepo;
    

    @Bean
    public RouterFunction<?> localeResourceRoutes() {
        return route(
                GET(buildURL("locale-resource", this::sysGetLocaleResourceListByCompanyIdAndLocale)),
                this::sysGetLocaleResourceListByCompanyIdAndLocale
            )
            .andRoute(GET(buildURL("locale-resource", this::sysGetUsedLanguages)), this::sysGetUsedLanguages)
            .andRoute(GET(buildURL("locale-resource", this::sysGetUsedLangTypeGroups)), this::sysGetUsedLangTypeGroups)
            .andRoute(GET(buildURL("locale-resource", this::sysGetUsedLangCategories)), this::sysGetUsedLangCategories)
            .andRoute(
                GET(buildURL("locale-resource", this::sysGetLocaleResourceByCompanyIdAndCatAndTypeGroup)),
                this::sysGetLocaleResourceByCompanyIdAndCatAndTypeGroup
            )
            .andRoute(GET(buildURL("locale-resource", this::sysGetAllLanguages)), this::sysGetAllLanguages)
            .andRoute(POST(buildURL("locale-resource", this::saveOrUpdateOrDelete)), this::saveOrUpdateOrDelete);
    }

    private Mono<ServerResponse> saveOrUpdateOrDelete(ServerRequest request) {
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
            .bodyToMono(LocalResourceReq.class)
            .flatMap(
                req -> {
                    var addFlux = Flux.empty();
                    if (req.getAddArray().size() > 0) {
                        addFlux =
                            Flux
                                .fromIterable(req.getAddArray())
                                .flatMap(
                                    lr -> {
                                        return super.saveEntity(mainRepo, lr, auth);
                                    }
                                );
                    }

                    var removeFlux = Flux
                        .fromIterable(req.getRemoveArray())
                        .flatMap(
                            lr -> {
                                return mainRepo
                                    .findByComanyIdCategoryTypeGroupKeyAndLocale(
                                        lr.getCompanyId(),
                                        lr.getCategory(),
                                        lr.getTypeGroup(),
                                        lr.getKey(),
                                        lr.getLocale()
                                    )
                                    .flatMap(
                                        foundLocaleResource -> {
                                            return super.softDeleteEntity(mainRepo, foundLocaleResource, auth);
                                        }
                                    );
                            }
                        );

                    var editFlux = Flux
                        .fromIterable(req.getEditArray())
                        .flatMap(
                            lr -> {
                                return mainRepo
                                    .findByComanyIdCategoryTypeGroupKeyAndLocale(
                                        lr.getCompanyId(),
                                        lr.getCategory(),
                                        lr.getTypeGroup(),
                                        lr.getKey(),
                                        lr.getLocale()
                                    )
                                    .flatMap(
                                        foundLocaleResource -> {
                                            foundLocaleResource.setValue(lr.getNewValue());
                                            return super.updateEntity(mainRepo, foundLocaleResource, auth);
                                        }
                                    )
                                    .switchIfEmpty(saveLocaleResource(lr));
                            }
                        );

                    return Flux
                        .merge(addFlux, removeFlux, editFlux)
                        .collectList()
                        .flatMap(item -> ok(item, List.class))
                        .onErrorResume(e -> error(e));
                }
            );
    }

    private Mono<LocaleResource> saveLocaleResource(LocaleResource lr) {
        lr.setValue(lr.getNewValue());
        return saveEntity(mainRepo, lr, auth);
    }

    private Mono<ServerResponse> sysGetUsedLangTypeGroups(ServerRequest request) {
        // SYSTEM BLOCK CODE
        // PLEASE DO NOT EDIT
        if (request == null) {
            String methodName = new Object() {}.getClass().getEnclosingMethod().getName();
            return Mono.just(new MyServerResponse(methodName));
        }
        // END SYSTEM BLOCK CODE

        var textSearch = getParam(request, "textSearch");

        if (textSearch != null && textSearch.trim().length() == 0) {
            textSearch = null;
        }
        try {
            return customRepo
                .sysGetUsedLangTypeGroups(textSearch)
                .flatMap(item -> ok(item))
                .onErrorResume(e -> error(e));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private Mono<ServerResponse> sysGetUsedLangCategories(ServerRequest request) {
        // SYSTEM BLOCK CODE
        // PLEASE DO NOT EDIT
        if (request == null) {
            String methodName = new Object() {}.getClass().getEnclosingMethod().getName();
            return Mono.just(new MyServerResponse(methodName));
        }
        // END SYSTEM BLOCK CODE

        var textSearch = getParam(request, "textSearch");

        if (textSearch != null && textSearch.trim().length() == 0) {
            textSearch = null;
        }

        try {
            return customRepo
                .sysGetUsedLangCategories(textSearch)
                .flatMap(item -> ok(item))
                .onErrorResume(e -> error(e));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private Mono<ServerResponse> sysGetLocaleResourceByCompanyIdAndCatAndTypeGroup(ServerRequest request) {
        // SYSTEM BLOCK CODE
        // PLEASE DO NOT EDIT
        if (request == null) {
            String methodName = new Object() {}.getClass().getEnclosingMethod().getName();
            return Mono.just(new MyServerResponse(methodName));
        }
        // END SYSTEM BLOCK CODE

        var companyIdStr = request.queryParam("companyId").orElse(null);
        var category = request.queryParam("category").orElse(null);
        var typeGroup = request.queryParam("typeGroup").orElse(null);
        var textSearch = request.queryParam("textSearch").orElse(null);

        Long companyId = null;

        try {
            if (companyIdStr != null && !"null".equals(companyIdStr)) companyId = Long.parseLong(companyIdStr);
            if (companyId == null) return badRequest().bodyValue("SYS.MSG.INVILID_COMPANY_ID");
        } catch (Exception e) {
            e.printStackTrace();
            return badRequest().bodyValue("SYS.MSG.INVILID_COMPANY_ID");
        }

        if ((category != null && category.trim().length() == 0) || "null".equals(category)) category = null;

        if ((typeGroup != null && typeGroup.trim().length() == 0) || "null".equals(typeGroup)) typeGroup = null;

        if ((textSearch != null && textSearch.trim().length() == 0) || "null".equals(textSearch)) textSearch = null;

        Long page, pageSize;
        try {
            page = getLongParam(request, "page", 1L);
            pageSize = getLongParam(request, "pageSize", -1L);
        } catch (Exception e) {
            return badRequest().bodyValue(Message.INVALID_TABLE_NAME);
        }

        try {
            return customRepo
                .sysGetLocaleResourceByCompanyIdAndCatAndTypeGroup(
                    companyId,
                    category,
                    typeGroup,
                    textSearch,
                    page,
                    pageSize
                )
                .flatMap(item -> ok(item))
                .onErrorResume(e -> error(e));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private Mono<ServerResponse> sysGetLocaleResourceListByCompanyIdAndLocale(ServerRequest request) {
        // SYSTEM BLOCK CODE
        // PLEASE DO NOT EDIT
        if (request == null) {
            String methodName = new Object() {}.getClass().getEnclosingMethod().getName();
            return Mono.just(new MyServerResponse(methodName));
        }
        // END SYSTEM BLOCK CODE

        var companyIdStr = request.queryParam("companyId").orElse(null);
        var localeStr = request.queryParam("locale").orElse("vi-VN");

        Long companyId = null;
        Boolean includeDeleted = false, includeDisabled = false;

        try {
            if (companyIdStr != null && !"null".equals(companyIdStr)) companyId = Long.parseLong(companyIdStr);
        } catch (Exception e) {
           
        }

        try {
            includeDeleted = super.getIncludeDeleted(request);
            includeDisabled = super.getIncludeDisabled(request);
        } catch (Exception e) {
            e.printStackTrace();
            return badRequest().bodyValue(e.getMessage());
        }

        try {
        	if (companyId == null) {
        		var _includeDeleted = includeDeleted;
        		var _includeDisabled = includeDisabled;
        		return ownerOrgRepo.findFirstCompanyId().flatMap(foundCompanyId -> {
        			System.out.println("foundCompanyId");
        			System.out.println(foundCompanyId);
        			return customRepo
                            .sysGetLocaleResourceListByCompanyIdAndLocale(foundCompanyId, localeStr, _includeDeleted, _includeDisabled)
                            .flatMap(item -> ok(item))
                            .onErrorResume(e -> error(e));
        		});
        	} else {
        		return customRepo
                    .sysGetLocaleResourceListByCompanyIdAndLocale(companyId, localeStr, includeDeleted, includeDisabled)
                    .flatMap(item -> ok(item))
                    .onErrorResume(e -> error(e));
        	}
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private Mono<ServerResponse> sysGetAllLanguages(ServerRequest request) {
        // SYSTEM BLOCK CODE
        // PLEASE DO NOT EDIT
        if (request == null) {
            String methodName = new Object() {}.getClass().getEnclosingMethod().getName();
            return Mono.just(new MyServerResponse(methodName));
        }
        // END SYSTEM BLOCK CODE

        Boolean includeDeleted = false, includeDisabled = false;
        try {
            includeDeleted = super.getIncludeDeleted(request);
            includeDisabled = super.getIncludeDisabled(request);
        } catch (Exception e) {
            e.printStackTrace();
            return badRequest().bodyValue(e.getMessage());
        }

        try {
            return customRepo
                .sysGetAllLanguages(includeDeleted, includeDisabled)
                .flatMap(item -> ok(item))
                .onErrorResume(e -> error(e));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private Mono<ServerResponse> sysGetUsedLanguages(ServerRequest request) {
        // SYSTEM BLOCK CODE
        // PLEASE DO NOT EDIT
        if (request == null) {
            String methodName = new Object() {}.getClass().getEnclosingMethod().getName();
            return Mono.just(new MyServerResponse(methodName));
        }
        // END SYSTEM BLOCK CODE

        try {
            return customRepo.sysGetUsedLanguages().flatMap(item -> ok(item)).onErrorResume(e -> error(e));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
