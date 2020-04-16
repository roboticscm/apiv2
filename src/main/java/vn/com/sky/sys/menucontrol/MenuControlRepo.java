package vn.com.sky.sys.menucontrol;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;
import vn.com.sky.sys.model.MenuControl;

public interface MenuControlRepo extends ReactiveCrudRepository<MenuControl, Long> {
    @Query("select * from menu_control where deleted_by is null and menu_id = :menuId and control_id = :controlId")
    public Mono<MenuControl> findByMenuIdAndControlId(Long menuId, Long controlId);

    @Query(
        "select * from menu_control where deleted_by is null and menu_id in (select id from menu where path = :menuPath and deleted_by is null and disabled = false) and control_id = :controlId"
    )
    public Mono<MenuControl> findByMenuPathAndControlId(String menuPath, Long controlId);
}
