package vn.com.sky.sys.usersettings;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import vn.com.sky.sys.model.UserSettings;

@Repository
public interface UserSettingsRepo extends ReactiveCrudRepository<UserSettings, Long> {
    @Query(
        "select * from user_settings where user_id = :userId and menu_path = :menuPath and control_id = :controlId and key = :key limit 1"
    )
    public Mono<UserSettings> findByUserIdAndMenuPathAndControlIdAndKey(
        Long userId,
        String menuPath,
        String controlId,
        String key
    );

    @Query("select * from user_settings where user_id = :userId and menu_path = :menuPath and control_id = :controlId")
    public Flux<UserSettings> findByUserIdAndMenuPathAndControlId(Long userId, String menuPath, String controlId);
}
