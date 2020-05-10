package vn.com.sky.task.task;

import java.util.List;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

import reactor.core.publisher.Mono;
import vn.com.sky.task.model.TskStatusDetail;

@Repository
public interface TskStatusDetailRepo extends ReactiveCrudRepository<TskStatusDetail, Long> {
	@Query("delete from tsk_status_detail where id in (:ids)")
	public Mono<Void> deleteByIds(List<Long> ids);
}
