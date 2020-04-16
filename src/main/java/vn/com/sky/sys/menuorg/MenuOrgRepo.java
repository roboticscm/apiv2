package vn.com.sky.sys.menuorg;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;
import vn.com.sky.sys.model.MenuOrg;

public interface MenuOrgRepo extends ReactiveCrudRepository<MenuOrg, Long> {
    @Query("select * from menu_org where deleted_by is null and menu_id=:menuId and org_id=:orgId")
    public Mono<MenuOrg> findByMenuIdAndOrgId(Long menuId, Long orgId);

    @Query("delete from menu_org where menu_id=:menuId and org_id=:orgId")
    public Mono<Void> deleteByMenuIdAndOrgId(Long menuId, Long orgId);
}
