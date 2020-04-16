package vn.com.sky.sys.usersettings;

import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;
import vn.com.sky.base.BaseR2dbcRepository;

@Repository
public class CustomUserSettingsRepo extends BaseR2dbcRepository {

    /*
	-- Module: System (sys)
	-- Section: User Settings (ust)
	-- Function Description: Get last department id, menu path and Language by user id
	-- Params:
	--  userId
	*/
    public Mono<String> sysGetUserSettings(Long userId, Long companyId) {
        String methodName = new Object() {}.getClass().getEnclosingMethod().getName();

        var ret =
            this.databaseClient()
                .execute(genSql(methodName, "userId", "companyId"))
                .bind("userId", userId)
                .bind("companyId", companyId);

        return ret.as(String.class).fetch().first();
    }
}
