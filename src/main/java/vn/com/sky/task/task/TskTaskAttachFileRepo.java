package vn.com.sky.task.task;

import java.util.ArrayList;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

import reactor.core.publisher.Mono;
import vn.com.sky.task.model.TskTaskAttachFile;

@Repository
public interface TskTaskAttachFileRepo extends ReactiveCrudRepository<TskTaskAttachFile, Long> {
	@Query("delete from tsk_task_attach_file where task_id = :taskId and file_name in (:fileNames)")
	public Mono<Void> deleteByTaskId(Long taskId, ArrayList<String> fileNames);

}
