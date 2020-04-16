package vn.com.sky.sys.rolecontrol;

import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;
import vn.com.sky.base.BaseR2dbcRepository;

@Repository
public class CustomRoleControlRepo extends BaseR2dbcRepository {

    /*
	-- Module: System (sys)
	-- Section: Role Control (rct)
	-- Function Description: Get Roled control list by department id and user id and menu path
	-- Params:
	--  userId
	--  menuPath
	*/
    public Mono<String> sysGetControlListByDepIdAndUserIdAndMenuPath(Long depId, Long userId, String menuPath) {
        String methodName = new Object() {}.getClass().getEnclosingMethod().getName();

        var ret =
            this.databaseClient()
                .execute(genSql(methodName, "depId", "userId", "menuPath"))
                .bind("userId", userId)
                .bind("menuPath", menuPath);

        if (depId != null) ret = ret.bind("depId", depId); else ret = ret.bindNull("depId", Long.class);

        return ret.as(String.class).fetch().first();
    }
}
