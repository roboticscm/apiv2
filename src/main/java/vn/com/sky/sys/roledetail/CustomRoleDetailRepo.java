package vn.com.sky.sys.roledetail;

import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;
import vn.com.sky.base.BaseR2dbcRepository;

@Repository
public class CustomRoleDetailRepo extends BaseR2dbcRepository {

    /*
	-- Module: System (sys)
	-- Section: Role Detail (rdt)
	-- Function Description: Get menu and role control list by owner_org_id and role_id
	-- Params:
	--  ownerOrgId
	--  roldId
	--  includeDeleted: Include deleted record
	--  includeDisabled: Include disabled record
	*/
    public Mono<String> sysGetMenuRoleControlList(
        Long ownerOrgId,
        Long roleId,
        Boolean includeDeleted,
        Boolean includeDisabled
    ) {
        String methodName = new Object() {}.getClass().getEnclosingMethod().getName();

        var ret =
            this.databaseClient()
                .execute(genSql(methodName, "ownerOrgId", "roleId", "includeDeleted", "includeDisabled"))
                .bind("ownerOrgId", ownerOrgId)
                .bind("roleId", roleId)
                .bind("includeDeleted", includeDeleted)
                .bind("includeDisabled", includeDisabled);

        return ret.as(String.class).fetch().first();
    }
}
