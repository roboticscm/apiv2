package vn.com.sky.sys.humanororg;

import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;
import vn.com.sky.base.BaseR2dbcRepository;

@Repository
public class CustomHumanOrOrgRepo extends BaseR2dbcRepository {

    /*
	-- Module: System (sys)
	-- Section: Human or org (hoo)
	-- Function Description: Get user list by org id (company id, branch id, ...)
	-- Params:
	--  _org_id: Owner org id
	--  _include_deleted: Include deleted record
	--  _include_disabled: Include disabled record
	*/
    public Mono<String> sysGetUserListByOrgId(Long orgId, Boolean includeDeleted, Boolean includeDisabled) {
        String methodName = new Object() {}.getClass().getEnclosingMethod().getName();

        var ret =
            this.databaseClient()
                .execute(genSql(methodName, "orgId", "includeDeleted", "includeDisabled"))
                .bind("includeDeleted", includeDeleted)
                .bind("includeDisabled", includeDisabled);

        if (orgId == null) ret = ret.bindNull("orgId", Long.class); else ret = ret.bind("orgId", orgId);

        return ret.as(String.class).fetch().first();
    }

    
    public Mono<String> sysGetUserInfoById(Long userId) {
        String methodName = new Object() {}.getClass().getEnclosingMethod().getName();

        var ret = this.databaseClient().execute(genSql(methodName, "userId")).bind("userId", userId);

        return ret.as(String.class).fetch().first();
    }
    
    
    
    public Mono<String> findAvatars(String userIds) {
        String methodName = new Object() {}.getClass().getEnclosingMethod().getName();

        var ret = this.databaseClient().execute(genSql(methodName, "userIds")).bind("userIds", userIds);

        return ret.as(String.class).fetch().first();
    }
    
}
