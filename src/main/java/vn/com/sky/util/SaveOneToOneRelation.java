package vn.com.sky.util;

import org.springframework.web.reactive.function.server.ServerRequest;

import reactor.core.publisher.Mono;
import vn.com.sky.base.GenericEntity;

public interface SaveOneToOneRelation<T extends GenericEntity> {
    public Mono<T> saveEntity(ServerRequest request, Long mainId, Long subId);
}
