package vn.com.sky.task.task;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

import reactor.core.publisher.Mono;
import vn.com.sky.task.model.TskTask;

@Repository
public interface TskTaskRepo extends ReactiveCrudRepository<TskTask, Long> {
	@Query("select exists (select from tsk_task where lower(name) = lower(:name) and ((project_id is null and :projectId is null) or (project_id = :projectId) )) ")
    public Mono<Boolean> isNameExisted(String name, Long projectId);
    
    @Query("select exists (select from tsk_task where lower(name) = lower(:name) and (project_id is null or project_id=:projectId) and id != :id)")
    public Mono<Boolean> isNameDuplicated(String name, Long projectId, Long id);
}
