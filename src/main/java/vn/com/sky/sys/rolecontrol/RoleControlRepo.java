package vn.com.sky.sys.rolecontrol;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;
import vn.com.sky.sys.model.RoleControl;

@Repository
public interface RoleControlRepo extends ReactiveCrudRepository<RoleControl, Long> {
    @Query(
        "select * from role_control where deleted_by is null and role_detail_id = :roleDetailId and menu_control_id in (select id from menu_control where menu_id=:menuId and control_id=:controlId)"
    )
    public Mono<RoleControl> findByRoleDetailIdAndMenuIdAndControlId(Long roleDetailId, Long menuId, Long controlId);

    @Query("delete from role_control where role_detail_id = :roleDetailId")
    public Mono<RoleControl> deleteByRoleDetailId(Long roleDetailId);
}
