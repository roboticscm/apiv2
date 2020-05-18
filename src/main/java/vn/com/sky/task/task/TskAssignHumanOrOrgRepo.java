package vn.com.sky.task.task;

import java.util.ArrayList;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import vn.com.sky.task.model.TskAssignHumanOrOrg;

@Repository
public interface TskAssignHumanOrOrgRepo extends ReactiveCrudRepository<TskAssignHumanOrOrg, Long> {
	@Query("DELETE FROM tsk_assign_human_or_org WHERE task_id = :taskId AND assign_position = :position AND human_or_org_id IN (:humanIds)")
	public Mono<Void> deleteByTaskIdAndPosition(Long taskId, String position,  ArrayList<Long> humanIds);

	@Query("SELECT distinct human_or_org_id FROM tsk_assign_human_or_org WHERE task_id = :taskId")
	public Flux<Long> findHumanIdsByTaskId(Long taskId);
}
