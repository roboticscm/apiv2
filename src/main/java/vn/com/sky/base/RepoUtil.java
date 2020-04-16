package vn.com.sky.base;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;
import vn.com.sky.security.AuthenticationManager;

public class RepoUtil {

    public static <T extends GenericEntity> Mono<T> save(
        ReactiveCrudRepository<T, Long> repo,
        T entity,
        AuthenticationManager auth
    ) {
        entity.createdBy(auth.getUserId());
        return repo.save(entity);
    }

    public static <T extends GenericEntity> Mono<T> update(
        ReactiveCrudRepository<T, Long> repo,
        T entity,
        AuthenticationManager auth
    ) {
        entity.updatedBy(auth.getUserId());
        return repo.save(entity);
    }

    public static <T extends GenericEntity> Mono<T> softDelete(
        ReactiveCrudRepository<T, Long> repo,
        T entity,
        AuthenticationManager auth
    ) {
        entity.deletedBy(auth.getUserId());
        return repo.save(entity);
    }
}
