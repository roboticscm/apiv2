package vn.com.sky.sys.assignmentrole;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;
import vn.com.sky.sys.model.AssignmentRole;

@Repository
public interface AssignmentRoleRepo extends ReactiveCrudRepository<AssignmentRole, Long> {
    @Query("select * from assignment_role where deleted_by is null and user_id = :userId and role_id = :roleId")
    public Mono<AssignmentRole> findByUserIdAndRoleId(Long userId, Long roleId);
}
