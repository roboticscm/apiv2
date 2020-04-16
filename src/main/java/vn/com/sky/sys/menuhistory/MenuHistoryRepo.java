package vn.com.sky.sys.menuhistory;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;
import vn.com.sky.sys.model.MenuHistory;

@Repository
public interface MenuHistoryRepo extends ReactiveCrudRepository<MenuHistory, Long> {
    @Query(
        "select *" +
        " from menu_history" +
        " where human_id = :userId" +
        " and dep_id = :depId " +
        " and menu_id in (select id from menu where deleted_by is null and disabled = false and path=:menuPath)"
    )
    public Mono<MenuHistory> findByUserIdDepIdAndMenuPath(Long userId, Long depId, String menuPath);
}
