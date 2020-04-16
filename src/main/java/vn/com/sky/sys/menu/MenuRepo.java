package vn.com.sky.sys.menu;

import java.util.List;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;
import vn.com.sky.sys.model.Menu;

@Repository
public interface MenuRepo extends ReactiveCrudRepository<Menu, Long> {
    @Query("delete from menu where id in (:ids)")
    public Mono<Void> deleteByIds(List<Long> ids);

    @Query("select * from menu where deleted_by is null and disabled = false and path = :path")
    public Mono<Menu> findByPath(String path);
}
