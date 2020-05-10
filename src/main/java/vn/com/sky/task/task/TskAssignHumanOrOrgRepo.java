package vn.com.sky.task.task;

import java.util.ArrayList;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

import reactor.core.publisher.Mono;
import vn.com.sky.task.model.TskAssignHumanOrOrg;

@Repository
public interface TskAssignHumanOrOrgRepo extends ReactiveCrudRepository<TskAssignHumanOrOrg, Long> {
	@Query("delete from tsk_assign_human_or_org where task_id = :taskId and assign_position = :position and human_or_org_id in (:humanIds)")
	public Mono<Void> deleteByTaskIdAndPosition(Long taskId, String position,  ArrayList<Long> humanIds);
}
