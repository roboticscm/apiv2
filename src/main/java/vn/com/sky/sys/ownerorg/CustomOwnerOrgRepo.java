package vn.com.sky.sys.ownerorg;

import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;
import vn.com.sky.base.BaseR2dbcRepository;

@Repository
public class CustomOwnerOrgRepo extends BaseR2dbcRepository {

    /*
	-- Module: System (sys)
	-- Section: Owner Org (ono)
	-- Function Description: Get owner org tree
	-- Params:
	--  includeDeleted: Include deleted record
	--  includeDisabled: Include disabled record
	
	*/
    public Mono<String> sysGetOwnerOrgTree(Long parentId, Boolean includeDeleted, Boolean includeDisabled) {
        String methodName = new Object() {}.getClass().getEnclosingMethod().getName();

        var ret =
            this.databaseClient()
                .execute(genSql(methodName, "parentId",  "includeDeleted", "includeDisabled"))
                .bind("includeDeleted", includeDeleted)
                .bind("includeDisabled", includeDisabled);
        
        if (parentId == null) {
        	ret = ret.bindNull("parentId", Long.class);
        } else {
        	ret = ret.bind("parentId", parentId);
        }

        return ret.as(String.class).fetch().first();
    }

    /*
	-- Module: System (sys)
	-- Section: Owner Org (ono)
	-- Function Description: Get owner org with role tree
	-- Params:
	--  includeDeleted: Include deleted record
	--  includeDisabled: Include disabled record
	*/
    public Mono<String> sysGetOwnerOrgRoleTree(Boolean includeDeleted, Boolean includeDisabled) {
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
	-- Section: Owner Org (ono)
	-- Function Description: Get owner org with human tree
	-- Params:
	--  includeDeleted: Include deleted record
	--  includeDisabled: Include disabled record
	*/
    public Mono<String> sysGetOwnerOrgHumanTree(String filter, String excludeHumanIds, Boolean includeDeleted, Boolean includeDisabled) {
        String methodName = new Object() {}.getClass().getEnclosingMethod().getName();

        var ret =
            this.databaseClient()
                .execute(genSql(methodName, "filter", "excludeHumanIds", "includeDeleted", "includeDisabled"))
                .bind("includeDeleted", includeDeleted)
                .bind("includeDisabled", includeDisabled);

        if (filter != null) {
        	ret = ret.bind("filter", filter);
        } else {
        	ret = ret.bindNull("filter", String.class);
        }
        
        
        if (excludeHumanIds != null) {
        	ret = ret.bind("excludeHumanIds", excludeHumanIds);
        } else {
        	ret = ret.bindNull("excludeHumanIds", String.class);
        }
        
        return ret.as(String.class).fetch().first();
    }
    
    /*
	-- Module: System (sys)
	-- Section: Owner Org (ono)
	-- Function Description: Get assigned role department list by user id
	-- Params:
	--  userId
	--  includeDeleted: Include deleted record
	--  includeDisabled: Include disabled record
	*/
    public Mono<String> sysGetRoledDepartmentListByUserId(
        Long userId,
        Boolean includeDeleted,
        Boolean includeDisabled
    ) {
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
	-- Section: Owner Org (ono)
	-- Function Description: Get Department tree by menu id
	-- Params:
	--  menuId
	*/
    public Mono<String> sysGetDepartmentTreeByMenuId(Long menuId) {
        String methodName = new Object() {}.getClass().getEnclosingMethod().getName();

        var ret = this.databaseClient().execute(genSql(methodName, "menuId")).bind("menuId", menuId);

        return ret.as(String.class).fetch().first();
    }

    /*
	-- Module: System (sys)
	-- Section: Owner Org (ono)
	-- Function Get Available department tree ready for assignment menu
	-- Params:
	--  menuId
	*/
    public Mono<String> sysGetAvailableDepartmentTreeForMenu(Long menuId) {
        String methodName = new Object() {}.getClass().getEnclosingMethod().getName();

        var ret = this.databaseClient().execute(genSql(methodName, "menuId"));

        if (menuId == null) ret = ret.bindNull("menuId", Long.class); else ret = ret.bind("menuId", menuId);

        return ret.as(String.class).fetch().first();
    }

    /*
   	-- Module: System (sys)
   	-- Section: Owner Org (ono)
   	-- Function Get Human Org Tree
   	-- Params:
   	--  humanId
   	*/
    public Mono<String> sysGetHumanOrgTree(Long humanId) {
        String methodName = new Object() {}.getClass().getEnclosingMethod().getName();

        var ret = this.databaseClient().execute(genSql(methodName, "humanId"));

        if (humanId == null) {
            ret = ret.bindNull("humanId", Long.class);
        } else {
            ret = ret.bind("humanId", humanId);
        }

        return ret.as(String.class).fetch().first();
    }

    /*
   	-- Module: System (sys)
   	-- Section: Owner Org (ono)
   	-- Function Get Human Org Tree
   	-- Params:
   	--  humanId
   	*/
    public Mono<String> sysGetAssignedHumanOrgTree(Long humanId) {
        String methodName = new Object() {}.getClass().getEnclosingMethod().getName();

        var ret = this.databaseClient().execute(genSql(methodName, "humanId")).bind("humanId", humanId);

        return ret.as(String.class).fetch().first();
    }
    
    
    
    public Mono<Long> sysGetFirstRoledDepId(Long userId, String menuPath) {
        String methodName = new Object() {}.getClass().getEnclosingMethod().getName();

        var ret = this.databaseClient()
        		.execute(genSql(methodName, "userId", "menuPath"))
        		.bind("userId", userId)
        		.bind("menuPath", menuPath);

        return ret.as(Long.class)
        		.fetch()
        		.first();
    }
   
}
