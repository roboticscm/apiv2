package vn.com.sky.sys.globalparam;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;
import vn.com.sky.sys.model.GlobalParam;

@Repository
public interface GlobalParamRepo extends ReactiveCrudRepository<GlobalParam, Long> {
    @Query("select * from global_param where key = :key")
    public Mono<GlobalParam> findByKey(String key);
}
