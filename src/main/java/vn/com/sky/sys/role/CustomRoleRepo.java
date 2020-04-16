package vn.com.sky.sys.role;

import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;
import vn.com.sky.base.BaseR2dbcRepository;

@Repository
public class CustomRoleRepo extends BaseR2dbcRepository {

    /*
	-- Module: System (sys)
	-- Section: Role (rle)
	-- Function Description: Get role list by org id
	-- Params:
	--  _org_id
	--  _include_deleted: Include deleted record
	--  _include_disabled: Include disabled record
	*/
    public Mono<String> sysGetRoleListByOrgId(Long orgId, Boolean includeDeleted, Boolean includeDisabled) {
        String methodName = new Object() {}.getClass().getEnclosingMethod().getName();

        var ret =
            this.databaseClient()
                .execute(genSql(methodName, "orgId", "includeDeleted", "includeDisabled"))
                .bind("includeDeleted", includeDeleted)
                .bind("includeDisabled", includeDisabled);

        if (orgId == null) ret = ret.bindNull("orgId", Long.class); else ret = ret.bind("orgId", orgId);

        return ret.as(String.class).fetch().first();
    }
}
