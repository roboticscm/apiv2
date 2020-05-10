package vn.com.sky.task.task;

import java.util.ArrayList;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

import reactor.core.publisher.Mono;
import vn.com.sky.task.model.TskAssignOwnerOrg;

@Repository
public interface TskAssignOwnerOrgRepo extends ReactiveCrudRepository<TskAssignOwnerOrg, Long> {
	@Query("delete from tsk_assign_owner_org where task_id = :taskId and assign_position = :position and owner_org_id in (:ownerOrgIds)")
	public Mono<Void> deleteByTaskIdAndPosition(Long taskId, String position,  ArrayList<Long> ownerOrgIds);
}
