package vn.com.sky.sys.roledetail;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;
import vn.com.sky.sys.model.RoleDetail;

@Repository
public interface RoleDetailRepo extends ReactiveCrudRepository<RoleDetail, Long> {
    @Query(
        "select * from role_detail where deleted_by is null and role_id=:roleId and menu_org_id in (select id from menu_org where org_id=:departmentId and menu_id=:menuId)"
    )
    public Mono<RoleDetail> findByRoleIdAndDepartmentIdAndMenuId(Long roleId, Long departmentId, Long menuId);
}
