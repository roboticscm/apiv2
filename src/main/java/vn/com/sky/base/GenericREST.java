package vn.com.sky.base;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.HandlerFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import vn.com.sky.Constants;
import vn.com.sky.util.CustomRepoUtil;
import vn.com.sky.util.MyServerResponse;
import vn.com.sky.util.OneToOneRepo;
import vn.com.sky.util.SDate;
import vn.com.sky.util.SaveOneToOneRelation;
import vn.com.sky.util.StringUtil;

/**
 * @author roboticscm2018@gmail.com (khai.lv) Created date: Apr 11, 2019
 */
//@Service
public class GenericREST {
    @Autowired
    protected Validator validator;
    @Autowired
    protected CustomRepoUtil utilRepo;
    
    public Long getCurrentDateTime() {
        return SDate.now();
    }

    protected Boolean getIncludeDeleted(ServerRequest request) throws Exception {
        var includeDeletedStr = request.queryParam("includeDeleted").orElse("false");
        Boolean includeDeleted = false;

        try {
            includeDeleted = Boolean.parseBoolean(includeDeletedStr);
            return includeDeleted;
        } catch (Exception e) {
            throw new Exception("{\"includeDeleted\":" + "\"SYS.MSG.INVILID_INCLUDE_DELETED\"}");
        }
    }

    protected Boolean getIncludeDisabled(ServerRequest request) throws Exception {
        var includeDisabledStr = request.queryParam("includeDisabled").orElse("false");
        Boolean includeDisabled = false;

        try {
            includeDisabled = Boolean.parseBoolean(includeDisabledStr);
            return includeDisabled;
        } catch (Exception e) {
            throw new Exception("{\"includeDeleted\":" + "\"SYS.MSG.INVILID_INCLUDE_DISABLED\"}");
        }
    }

    protected Mono<ServerResponse> ok(Object item) {
        return ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).body(Mono.just(item), String.class);
    }

    protected <T> Mono<ServerResponse> ok(Object item, Class<T> clazz) {
        return ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).body(Mono.just(item), clazz);
    }

    protected Mono<ServerResponse> error(Throwable e) {
        e.printStackTrace();
        return Mono
            .just(((Exception) e).getMessage())
            .flatMap(
                errorRes ->
                    ServerResponse.badRequest().contentType(MediaType.APPLICATION_JSON).bodyValue(e.getMessage())
            );
    }

    protected Mono<ServerResponse> error(String e) {
        return ServerResponse.badRequest().contentType(MediaType.APPLICATION_JSON).bodyValue(e);
    }

    protected Mono<ServerResponse> error(String field, String message) {
        return ServerResponse
            .badRequest()
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("{\"" + field + "\":" + "\"" + message + "\"}");
    }

    protected Mono<ServerResponse> error(HashMap<String, String> hmError) {
        ObjectMapper mapperObj = new ObjectMapper();
        try {
            return error(mapperObj.writeValueAsString(hmError));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }
    }

    protected String buildURL(String func, HandlerFunction<?> handlerFunction) {
        return buildURL(Constants.API_PREFIX, func, handlerFunction);
    }
    
    protected String buildURL(String prefix, String func, HandlerFunction<?> handlerFunction) {
        try {
            var methodName = ((MyServerResponse) handlerFunction.handle(null).block()).body;
            return prefix + (func.length() > 0 ? func + "/" : "") + StringUtil.toSnackCase(methodName, "-");
        } catch (ClassCastException e) {
            System.out.println("Missng SYSTEM BLOCK CODE in " + handlerFunction);
            return null;
        }
    }

    protected <T extends GenericEntity> String getTableName (T entity) {
    	return StringUtil.toSnackCase(entity.getClass().getSimpleName(), "_").substring(1);
    }
    
//    protected <T extends GenericEntity> Mono<T> saveEntity(
//            ReactiveCrudRepository<T, Long> repo,
//            T entity,
//            AuthenticationManager auth
//        ) {
//            entity.createdBy(auth != null ? auth.getUserId() : null);
////            entity.setCreatedDate(SDate.now());
////            entity.setVersion(1);
////            entity.setDisabled(false);
//
//            if (entity instanceof SortableEntity) {
//            	var tableName = getTableName(entity);
//            	return utilRepo.getMaxSort(tableName).flatMap(maxSort -> {
//            		((SortableEntity) entity).setSort(maxSort + 1);
//            		return repo.save(entity);
//            	});
//            	
//            } else {
//            	return repo.save(entity);
//            }
//            
//        }
    
    protected <T extends GenericEntity> Mono<T> saveEntity(
            ReactiveCrudRepository<T, Long> repo,
            T entity,
            Long userId
        ) {
            entity.createdBy(userId);

            if (entity instanceof SortableEntity) {
            	var tableName = getTableName(entity);
            	return utilRepo.getMaxSort(tableName).flatMap(maxSort -> {
            		((SortableEntity) entity).setSort(maxSort + 1);
            		return repo.save(entity);
            	});
            	
            } else {
            	return repo.save(entity);
            }
            
        }
    
//    protected <T extends GenericEntity> Mono<T> updateEntity(
//        ReactiveCrudRepository<T, Long> repo,
//        T entity,
//        AuthenticationManager auth
//    ) {
//        entity.updatedBy(auth != null ? auth.getUserId() : null);
//        
//        return repo.save(entity);
//    }

    protected <T extends GenericEntity> Mono<T> updateEntity(
            ReactiveCrudRepository<T, Long> repo,
            T entity,
            Long userId
        ) {
            entity.updatedBy(userId);
            
            return repo.save(entity);
        }
    
//    protected <T extends GenericEntity> Mono<T> softDeleteEntity(
//        ReactiveCrudRepository<T, Long> repo,
//        T entity,
//        AuthenticationManager auth
//    ) {
//        entity.deletedBy(auth.getUserId());
//        return repo.save(entity);
//    }
    
    
    protected <T extends GenericEntity> Mono<T> softDeleteEntity(
            ReactiveCrudRepository<T, Long> repo,
            T entity,
            Long userId
        ) {
            entity.deletedBy(userId);
            return repo.save(entity);
        }

    protected HashMap<String, String> defaultValidate(GenericEntity entity) {
        Iterator<ConstraintViolation<GenericEntity>> it = validator.validate(entity).iterator();
        var hmError = new LinkedHashMap<String, String>();
        while (it.hasNext()) {
            ConstraintViolation<GenericEntity> constraintViolation = it.next();
            hmError.put(constraintViolation.getPropertyPath().toString(), constraintViolation.getMessage());
        }
        return hmError;
    }

    protected Mono<ServerResponse> validate(GenericEntity entity) {
        var errors = defaultValidate(entity);
        if (errors != null && errors.size() > 0) {
            try {
                ObjectMapper mapperObj = new ObjectMapper();
                return error(mapperObj.writeValueAsString(errors));
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    protected List<Long> getIdsFromQueryParam(ServerRequest request) throws Exception {
        var strIds = request.queryParam("ids").orElse("");
        if (strIds.isBlank()) {
            throw new Exception("SYS.MSG.INVALID_ID_FORMAT");
        }

        try {
            return Stream.of(strIds.split(",")).map(Long::valueOf).collect(Collectors.toList());
        } catch (Exception e) {
            throw new Exception("SYS.MSG.INVALID_ID_FORMAT");
        }
    }

    protected String getParam(ServerRequest request, String paramName, String defaultValue) {
    	
        var value = URLDecoder.decode(request.queryParam(paramName).orElse(defaultValue), StandardCharsets.UTF_8);
        
        if(StringUtil.isBlank(value)) {
        	value = null;
        }
        
        return value;
    }

    protected String getParam(ServerRequest request, String paramName) {
        return getParam(request, paramName, "");
    }

    protected Long getLongParam(ServerRequest request, String paramName, Long defaultValue) throws Exception {
        var value = URLDecoder.decode(
            request.queryParam(paramName).orElse(defaultValue != null ? defaultValue.toString() : ""),
            StandardCharsets.UTF_8
        );

        try {
        	if (StringUtil.isBlank(value)) {
        		return defaultValue;
        	} else {
        		return Long.parseLong(value);
        	}
            
        } catch (Exception e) {
            throw e;
        }
    }

    protected Integer getIntParam(ServerRequest request, String paramName, Integer defaultValue) throws Exception {
        var value = URLDecoder.decode(
            request.queryParam(paramName).orElse(defaultValue != null ? defaultValue.toString() : ""),
            StandardCharsets.UTF_8
        );

        try {
        	if (StringUtil.isBlank(value)) {
        		return defaultValue;
        	} else {
        		return Integer.parseInt(value);
        	}
            
        } catch (Exception e) {
            throw e;
        }
    }
    
    protected Integer getIntParam(ServerRequest request, String paramName) throws Exception {
        return getIntParam(request, paramName, null);
    }
    
    protected Long getLongParam(ServerRequest request, String paramName) throws Exception {
        return getLongParam(request, paramName, null);
    }

    protected Boolean getBoolParam(ServerRequest request, String paramName, Boolean defaultValue) throws Exception {
        var value = URLDecoder.decode(
            request.queryParam(paramName).orElse(defaultValue != null ? defaultValue.toString() : ""),
            StandardCharsets.UTF_8
        );
        
        try {
        	if(StringUtil.isBlank(value)) {
        		return defaultValue;
        	} else {
        		return Boolean.parseBoolean(value);
        	}
            
        } catch (Exception e) {
            throw e;
        }
    }

    protected Boolean getBoolParam(ServerRequest request, String paramName) throws Exception {
        return getBoolParam(request, paramName, null);
    }

    protected <T extends GenericEntity> Mono<List<T>> saveManyRelation(
        OneToOneRepo<T> repo,
        Long mainId,
        ArrayList<Long> subIds,
        SaveOneToOneRelation<T> saveOneToOne,
        ServerRequest request
    ) {
        if (subIds == null) {
            return Mono.empty();
        } else {
            return Flux
                .fromIterable(subIds)
                .flatMap(
                    subId -> {
                        return repo
                            .findRelation(mainId, subId)
                            .flatMap(
                                found -> {
                                    return updateEntity(repo, found, getUserId(request));
                                }
                            )
                            .switchIfEmpty(saveOneToOne.saveEntity(request, mainId, subId));
                    }
                )
                .collectList();
        }
    }

    protected <T extends GenericEntity> Mono<List<Void>> deleteManyRelation(
        OneToOneRepo<T> repo,
        Long mainId,
        ArrayList<Long> subIds
    ) {
        if (subIds == null) {
            return Mono.empty();
        } else {
            return Flux
                .fromIterable(subIds)
                .flatMap(
                    subId -> {
                        return repo.deleteRelation(mainId, subId);
                    }
                )
                .collectList();
        }
    }
    
    protected Long getUserId(ServerRequest request) {
    	var auth = request.headers().header("Authorization").get(0);
    	var index = auth.indexOf("||| ");
        var userId = auth.substring(0, index);
    	return Long.parseLong(userId);
    }
}
