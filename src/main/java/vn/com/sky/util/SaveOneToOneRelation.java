package vn.com.sky.util;

import reactor.core.publisher.Mono;
import vn.com.sky.base.GenericEntity;

public interface SaveOneToOneRelation<T extends GenericEntity> {
    public Mono<T> saveEntity(Long mainId, Long subId);
}
