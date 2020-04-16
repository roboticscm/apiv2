package vn.com.sky.sys.menu;

import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;
import vn.com.sky.base.BaseR2dbcRepository;

@Repository
public class CustomMenuRepo extends BaseR2dbcRepository {

    /*
	-- Module: System (sys)
	-- Section: Menu (mnu)
	-- Function Description: Get assigned role menu list by user id and department id
	-- Params:
	--  userId
	--  depId: Department ID
	--  includeDeleted: Include deleted record
	--  includeDisabled: Include disabled record
	*/
    public Mono<String> sysGetRoledMenuListByUserIdAndDepId(
        Long userId,
        Long depId,
        Boolean includeDeleted,
        Boolean includeDisabled
    ) {
        String methodName = new Object() {}.getClass().getEnclosingMethod().getName();

        var ret =
            this.databaseClient()
                .execute(genSql(methodName, "userId", "depId", "includeDeleted", "includeDisabled"))
                .bind("userId", userId)
                .bind("includeDeleted", includeDeleted)
                .bind("includeDisabled", includeDisabled);

        if (depId != null) ret = ret.bind("depId", depId); else ret = ret.bindNull("depId", Long.class);

        return ret.as(String.class).fetch().first();
    }

    /*
	-- Module: System (sys)
	-- Section: Menu (mnu)
	-- Function Description: Get assigned role menu path list by user id 
	-- Params:
	--  userId
	--  includeDeleted: Include deleted record
	--  includeDisabled: Include disabled record
	*/
    public Mono<String> sysGetRoledMenuPathListByUserId(Long userId, Boolean includeDeleted, Boolean includeDisabled) {
        String methodName = new Object() {}.getClass().getEnclosingMethod().getName();

        var ret =
            this.databaseClient()
                .execute(genSql(methodName, "userId", "includeDeleted", "includeDisabled"))
                .bind("userId", userId)
                .bind("includeDeleted", includeDeleted)
                .bind("includeDisabled", includeDisabled);

        return ret.as(String.class).fetch().first();
    }

    /*
	-- Module: System (sys)
	-- Section: Menu (mnu)
	-- Function Description: Get the first assigned role menu path by user id and department id
	-- Params:
	--  userId
	--  depId: Department ID
	--  includeDeleted: Include deleted record
	--  includeDisabled: Include disabled record
	*/
    public Mono<String> sysGetFirstRoledMenuPathByUserIdAndDepId(
        Long userId,
        Long depId,
        Boolean includeDeleted,
        Boolean includeDisabled
    ) {
        String methodName = new Object() {}.getClass().getEnclosingMethod().getName();

        var ret =
            this.databaseClient()
                .execute(genSql(methodName, "userId", "depId", "includeDeleted", "includeDisabled"))
                .bind("userId", userId)
                .bind("depId", depId)
                .bind("includeDeleted", includeDeleted)
                .bind("includeDisabled", includeDisabled);

        return ret.as(String.class).fetch().first();
    }

    /*
	-- Module: System (sys)
	-- Section: Menu (mnu)
	-- Function Description: Get all menu list including deleted, disabled record
	-- Params: No
	*/
    public Mono<String> sysGetAllMenuList(Boolean sortByCreatedDate) {
        String methodName = new Object() {}.getClass().getEnclosingMethod().getName();

        var ret =
            this.databaseClient()
                .execute(genSql(methodName, "sortByCreatedDate"))
                .bind("sortByCreatedDate", sortByCreatedDate);

        return ret.as(String.class).fetch().first();
    }
}
