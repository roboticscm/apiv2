///**
// *
// */
//package vn.com.sky.base;
//
//import java.util.Calendar;
//import java.util.HashMap;
//import java.util.Iterator;
//import java.util.List;
//import java.util.stream.Collectors;
//import java.util.stream.Stream;
//
//import javax.validation.ConstraintViolation;
//import javax.validation.Validator;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.data.repository.reactive.ReactiveCrudRepository;
//import org.springframework.web.reactive.function.server.ServerRequest;
//
//import com.fasterxml.jackson.core.JsonProcessingException;
//import com.fasterxml.jackson.databind.ObjectMapper;
//
//import reactor.core.publisher.Flux;
//import reactor.core.publisher.Mono;
//import vn.com.sky.redis.LoginInfo;
//import vn.com.sky.redis.LoginInfoService;
//import vn.com.sky.security.SecurityContextRepository;
//
///**
// * @author roboticscm2018@gmail.com (khai.lv) Created date: Apr 18, 2019
// */
//abstract public class GenericService {
//	@Autowired
//	protected Validator validator;
//	@Autowired
//	protected LoginInfoService loginInfoService;
//
//	protected Long sort = 0l;
//
//	protected HashMap<String, String> validation(GenericEntity entity) {
//		Iterator<ConstraintViolation<GenericEntity>> it = validator.validate(entity).iterator();
//		var hsError = new HashMap<String, String>();
//		while (it.hasNext()) {
//			ConstraintViolation<GenericEntity> constraintViolation = it.next();
//			hsError.put(constraintViolation.getPropertyPath().toString(), constraintViolation.getMessage());
//		}
//		return hsError;
//	}
//
//	protected void initGenericFields(GenericEntity entity, Long userId) {
//		entity.setCreatedBy(userId);
//	}
//
//	protected List<Object> getIdsFromQueryParam(ServerRequest request) throws Exception {
//		var strIds = request.queryParam("id").orElse("");
//		if (strIds.isBlank()) {
//			throw new Exception("SYS.MSG.INVALID_ID");
//		}
//
//		try {
//			return Stream.of(strIds.split(",")).map(Long::valueOf).collect(Collectors.toList());
//		} catch (Exception e) {
//			throw new Exception("SYS.MSG.INVALID_ID_FORMAT");
//		}
//	}
//
//	abstract protected void saveOrUpdateElasticWithLog(boolean isUpdate, LoginInfo loginInfo, GenericEntity savedItem,
//			Object screen, String function);
//
//	abstract protected Mono<Object> save(ServerRequest request);
//
//	@SuppressWarnings({ "rawtypes", "unchecked" })
//	protected Mono<Object> save(ServerRequest request, ReactiveCrudRepository mainRepo, Class entityClass) {
//		var redisKey = SecurityContextRepository.getRequestPayload(request);
//		var screen = SecurityContextRepository.getRequestScreen(request);
//		var function = SecurityContextRepository.getRequestFunction(request);
//
//		return request.bodyToMono(entityClass).flatMap(entity -> {
//			var item = (GenericEntity) entity;
//			var errors = validation(item);
//			if (errors != null && errors.size() > 0) {
//				try {
//					ObjectMapper mapperObj = new ObjectMapper();
//					return Mono.error(new Exception(mapperObj.writeValueAsString(errors)));
//				} catch (JsonProcessingException e) {
//					e.printStackTrace();
//				}
//			}
//
//			return loginInfoService.getCurrent(redisKey).flatMap(loginInfo -> {
//				item.encryptProcessing();
//
//				if (item.getId() == null) {// create
//					initGenericFields(item, loginInfo.getUserId());
//					return mainRepo.save(item).map(savedItem -> {
//						saveOrUpdateElasticWithLog(false, loginInfo, (GenericEntity) savedItem, null, null);
//						return savedItem;
//					}).switchIfEmpty(Mono.error(new Exception("Error creating Entity")));
//				} else {// update
//					return mainRepo.findById(item.getId()).map(oldItem -> {
//						return (GenericEntity) updateGenericFields((GenericEntity) oldItem, item,
//								loginInfo.getUserId());
//					}).flatMap(newItem -> {
//						return mainRepo.save(newItem).map(savedItem -> {
//							saveOrUpdateElasticWithLog(true, loginInfo, (GenericEntity) savedItem, screen, function);
//							return savedItem;
//						});
//					}).switchIfEmpty(Mono.error(
//							new Exception("Error creating Entity. Maybe you specify ID for Auto increment column")));
//				}
//			}).collectList();
//		});
//	}
//
//	abstract protected void deleteLog(Iterable<Object> iterableIds, LoginInfo loginInfo, Object screen,
//			String function);
//
//	abstract protected Flux<Object> deleteById(ServerRequest request);
//
//	@SuppressWarnings({ "unchecked", "static-access", "rawtypes" })
//	protected Flux<Object> deleteById(ServerRequest request, ReactiveCrudRepository mainRepo) {
//		var redisKey = SecurityContextRepository.getRequestPayload(request);
//
//		Iterable<Object> iIds = null;
//		try {
//			iIds = getIdsFromQueryParam(request);
//		} catch (Exception e) {
//			return Flux.error(e);
//		}
//
//		var iterableIds = iIds;
//
//		var screen = SecurityContextRepository.getRequestScreen(request);
//		var function = SecurityContextRepository.getRequestFunction(request);
//
//		return loginInfoService.getCurrent(redisKey).flatMap(loginInfo -> {
//			return mainRepo.findAllById(iterableIds).flatMap(item -> {
//				var entity = (GenericEntity) item;
//				deleteGenericFields(entity, loginInfo.getUserId());
//
//				return mainRepo.save(entity);
//			}).then(Mono.create((callback) -> {
//				deleteLog(iterableIds, loginInfo, screen, function);
//				// save to elastic
//				callback.success();
//			}));
//
//		});
//	}
//
//	abstract protected void updateSortLog(GenericEntity item, LoginInfo loginInfo, Object screen, String function,
//			Long oldSortValue, Long newSortValue);
//
//	abstract protected Flux<Void> updateSort(ServerRequest request);
//
//	@SuppressWarnings({ "rawtypes", "unchecked" })
//	protected Flux<Void> updateSort(ServerRequest request, ReactiveCrudRepository mainRepo, String elasticRepoClass) {
//		var redisKey = SecurityContextRepository.getRequestPayload(request);
//
//		Iterable<Object> iIds = null;
//		try {
//			iIds = getIdsFromQueryParam(request);
//		} catch (Exception e) {
//			return Flux.error(e);
//		}
//
//		var iterableIds = iIds;
//
//		var screen = SecurityContextRepository.getRequestScreen(request);
//		var function = SecurityContextRepository.getRequestFunction(request);
//		sort = 0l;
//		return loginInfoService.getCurrent(redisKey).concatMap(loginInfo -> {
//			iterableIds.forEach(id -> {
//				mainRepo.findById(id).flatMap(item -> {
//					var lsort = ++sort;
//					if (item instanceof SortableEntity) {
//						Long oldSortValue = ((SortableEntity) item).getSort();
//						((SortableEntity) item).setSort(lsort);
//						updateSortLog((GenericEntity) item, loginInfo, screen, function, oldSortValue, lsort);
//					}
//
//
//					return mainRepo.save(item);
//				}).block();
//
//			});
//
//			// update to elastic
//
//			return Flux.empty();
//		});
//
//	}
//
//	protected GenericEntity updateGenericFields(GenericEntity oldEntity, GenericEntity entity, Long userId) {
//		entity.setUpdatedBy(userId);
//		entity.setUpdatedDate(Calendar.getInstance().getTimeInMillis());
//		entity.setCreatedBy(oldEntity.getCreatedBy());
//		entity.setCreatedDate(oldEntity.getCreatedDate());
//		entity.setDeletedBy(oldEntity.getDeletedBy());
//		entity.setDeletedDate(oldEntity.getDeletedDate());
//
//		if (entity instanceof SortableEntity) {
//			((SortableEntity) entity).setSort(((SortableEntity) oldEntity).getSort());
//		}
//
//		return entity;
//	}
//
//	protected void deleteGenericFields(GenericEntity entity, Long userId) {
//		entity.setDeletedBy(userId);
//		entity.setDeletedDate(Calendar.getInstance().getTimeInMillis());
//	}
//
//	protected void waitForResponse(String key) {
//
//	}
//}
