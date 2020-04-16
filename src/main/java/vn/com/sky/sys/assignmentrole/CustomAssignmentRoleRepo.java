package vn.com.sky.sys.assignmentrole;

import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;
import vn.com.sky.base.BaseR2dbcRepository;

@Repository
public class CustomAssignmentRoleRepo extends BaseR2dbcRepository {

    /*
	-- Module: System (sys)
	-- Section: Assignment Role (asr)
	-- Function Description: Get all assignment role and user list
	-- Params:
	--  includeDeleted: Include deleted record
	--  includeDisabled: Include disabled record
	*/
    public Mono<String> sysGetAllAssignmentRoleUserList(Boolean includeDeleted, Boolean includeDisabled) {
        String methodName = new Object() {}.getClass().getEnclosingMethod().getName();

        var ret =
            this.databaseClient()
                .execute(genSql(methodName, "includeDeleted", "includeDisabled"))
                .bind("includeDeleted", includeDeleted)
                .bind("includeDisabled", includeDisabled);

        return ret.as(String.class).fetch().first();
    }

    /*
	-- Module: System (sys)
	-- Section: Assignment Role (asr)
	-- Function Description: Get role id list of the user
	-- Params:
	--  userId: User Id
	--  includeDeleted: Include deleted record
	--  includeDisabled: Include disabled record
	*/
    public Mono<String> sysGetRoleListOfUser(Long userId, Boolean includeDeleted, Boolean includeDisabled) {
        String methodName = new Object() {}.getClass().getEnclosingMethod().getName();

        var ret =
            this.databaseClient()
                .execute(genSql(methodName, "userId", "includeDeleted", "includeDisabled"))
                .bind("includeDeleted", includeDeleted)
                .bind("includeDisabled", includeDisabled);

        if (userId == null) ret = ret.bindNull("userId", Long.class); else ret = ret.bind("userId", userId);

        return ret.as(String.class).fetch().first();
    }

    /*
	-- Module: System (sys)
	-- Section: Assignment Role (asr)
	-- Function Description: Get role id list of the many users
	-- Params:
	--  userIds: User Ids
	--  includeDeleted: Include deleted record
	--  includeDisabled: Include disabled record
	*/
    public Mono<String> sysGetRoleListOfUsers(String userIds, Boolean includeDeleted, Boolean includeDisabled) {
        String methodName = new Object() {}.getClass().getEnclosingMethod().getName();

        var ret =
            this.databaseClient()
                .execute(genSql(methodName, "userIds", "includeDeleted", "includeDisabled"))
                .bind("includeDeleted", includeDeleted)
                .bind("includeDisabled", includeDisabled);

        if (userIds == null) ret = ret.bindNull("userIds", String.class); else ret = ret.bind("userIds", userIds);

        return ret.as(String.class).fetch().first();
    }
}
