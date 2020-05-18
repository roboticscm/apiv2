package vn.com.sky.task.task;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import vn.com.sky.task.model.TskTask;

@Repository
public interface TskTaskRepo extends ReactiveCrudRepository<TskTask, Long> {
	@Query("select exists (select from tsk_task where lower(name) = lower(:name) and ((project_id is null and :projectId is null) or (project_id = :projectId) )) ")
    public Mono<Boolean> isNameExisted(String name, Long projectId);
    
    @Query("select exists (select from tsk_task where lower(name) = lower(:name) and (project_id is null or project_id=:projectId) and id != :id)")
    public Mono<Boolean> isNameDuplicated(String name, Long projectId, Long id);
    
    @Query("SELECT * " + 
    		"FROM tsk_task " + 
    		"WHERE submit_status = 1 AND (is_first_remindered = FALSE OR is_first_remindered IS NULL) AND " + 
    		" 	first_reminder BETWEEN :startTime AND :endTime ")
    public Flux<TskTask> findFirstReminders(Long startTime, Long endTime);
    
    
    @Query("SELECT * " + 
    		"FROM tsk_task " + 
    		"WHERE submit_status = 1 AND (is_second_remindered = FALSE OR is_second_remindered IS NULL) AND " + 
    		" 	second_reminder BETWEEN :startTime AND :endTime ")
    public Flux<TskTask> findSecondReminders(Long startTime, Long endTime);
}
