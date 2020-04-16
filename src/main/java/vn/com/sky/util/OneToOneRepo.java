package vn.com.sky.util;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;
import vn.com.sky.base.GenericEntity;

public interface OneToOneRepo<T extends GenericEntity> extends ReactiveCrudRepository<T, Long> {
    public Mono<T> findRelation(Long mainId, Long subId);

    public Mono<Void> deleteRelation(Long mainId, Long subId);
}
