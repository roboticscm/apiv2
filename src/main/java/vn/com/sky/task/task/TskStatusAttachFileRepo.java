package vn.com.sky.task.task;

import java.util.ArrayList;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

import reactor.core.publisher.Mono;
import vn.com.sky.task.model.TskStatusAttachFile;

@Repository
public interface TskStatusAttachFileRepo extends ReactiveCrudRepository<TskStatusAttachFile, Long> {
	@Query("delete from tsk_status_attach_file where status_detail_id = :statusDetailId and file_name in (:fileNames)")
	Mono<Void> deleteByStatusDetailId(Long statusDetailId, ArrayList<String> fileNames);
}
