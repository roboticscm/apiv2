package vn.com.sky.sys.tableutil;

import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;
import vn.com.sky.base.BaseR2dbcRepository;

@Repository
public class CustomTableUtilRepo extends BaseR2dbcRepository {

    /**
	-- Module: System (sys)
	-- Section: Table Util 
	-- Function Description: check if table has any deleted record
	-- Params:
	-- 	tableName
	--	onlyMe
	--  userId
	*/
    public Mono<String> hasAnyDeletedRecord(String tableName, Boolean onlyMe, Long userId) {
        String methodName = new Object() {}.getClass().getEnclosingMethod().getName();

        var ret =
            this.databaseClient()
                .execute(genSql(methodName, "tableName", "onlyMe", "userId"))
                .bind("tableName", tableName)
                .bind("onlyMe", onlyMe)
                .bind("userId", userId);

        return ret.as(String.class).fetch().first();
    }

    /**
	-- Module: System (sys)
	-- Section: Table Util
	-- Function Description: perform restore or forever delete record(s)
	-- Params:
	--  tableName
	--  deleteIds
	--  restoreIds
	--	updatedBy
	--	updatedDate
	*/
    public Mono<String> restoreOrForeverDelete(
        String tableName,
        String deleteIds,
        String restoreIds,
        Long updatedBy,
        Long updatedDate
    ) {
        String methodName = new Object() {}.getClass().getEnclosingMethod().getName();

        var ret =
            this.databaseClient()
                .execute(genSql(methodName, "tableName", "deleteIds", "restoreIds", "updatedBy", "updatedDate"))
                .bind("tableName", tableName)
                .bind("updatedBy", updatedBy)
                .bind("updatedDate", updatedDate);

        if (deleteIds == null) ret = ret.bindNull("deleteIds", String.class); else ret =
            ret.bind("deleteIds", deleteIds);

        if (restoreIds == null) ret = ret.bindNull("restoreIds", String.class); else ret =
            ret.bind("restoreIds", restoreIds);

        return ret.as(String.class).fetch().first();
    }

    /**
	-- Module: System (sys)
	-- Section: Table Util
	-- Function Description: Get all deleted record(s) of the table
	-- Params: 
	-- 	tableName
	--	onlyMe
	--  userId
	*/
    public Mono<String> getAllDeletedRecords(String tableName, String columns, Boolean onlyMe, Long userId) {
        String methodName = new Object() {}.getClass().getEnclosingMethod().getName();

        var ret =
            this.databaseClient()
                .execute(genSql(methodName, "tableName", "columns", "onlyMe", "userId"))
                .bind("tableName", tableName)
                .bind("columns", columns)
                .bind("onlyMe", onlyMe)
                .bind("userId", userId);

        return ret.as(String.class).fetch().first();
    }

    /**
	-- Module: System (sys)
	-- Section: Table Util
	-- Function Description: Get all record(s) of the table with pagination
	-- Params: 
	-- _table_name
	-- _columns
	-- _order_by
	-- _page
	-- _page_size
	-- _only_me: only records of user
	-- _user_id
	-- _include_disabled
	 */
    public Mono<String> getSimpleList(
        String tableName,
        String columns,
        String orderBy,
        Long page,
        Long pageSize,
        Boolean onlyMe,
        Long userId,
        Boolean includeDisabled
    ) {
        String methodName = new Object() {}.getClass().getEnclosingMethod().getName();

        var ret =
            this.databaseClient()
                .execute(
                    genSql(
                        methodName,
                        "tableName",
                        "columns",
                        "orderBy",
                        "page",
                        "pageSize",
                        "onlyMe",
                        "userId",
                        "includeDisabled"
                    )
                )
                .bind("tableName", tableName)
                .bind("columns", columns)
                .bind("orderBy", orderBy)
                .bind("page", page)
                .bind("pageSize", pageSize)
                .bind("onlyMe", onlyMe)
                .bind("userId", userId)
                .bind("includeDisabled", includeDisabled);

        return ret.as(String.class).fetch().first();
    }

    /**
	-- Module: System (sys)
	-- Section: Table Util
	-- Function Description: Get one record by id
	-- Params: 
	-- _table_name
	-- _id
	 */
    public Mono<String> getOneById(String tableName, Long id) {
        String methodName = new Object() {}.getClass().getEnclosingMethod().getName();

        var ret =
            this.databaseClient()
                .execute(genSql(methodName, "tableName", "id"))
                .bind("tableName", tableName)
                .bind("id", id);

        return ret.as(String.class).fetch().first();
    }

    /*
	-- Module: System (sys)
	-- Section: Table Util
	-- Function Description: Soft delete one or many record by id
	-- Params:
	--  tableName
	--  deletedIds
	--  userId
	--  deletedDate
	*/
    public Mono<String> softDeleteMany(String tableName, String deletedIds, Long userId, Long deletedDate) {
        String methodName = new Object() {}.getClass().getEnclosingMethod().getName();

        var ret =
            this.databaseClient()
                .execute(genSql(methodName, "tableName", "deletedIds", "userId", "deletedDate"))
                .bind("tableName", tableName)
                .bind("deletedIds", deletedIds)
                .bind("userId", userId)
                .bind("deletedDate", deletedDate);

        return ret.as(String.class).fetch().first();
    }

    public Mono<String> getAllColumnsOfTable(String tableName) {
        String methodName = new Object() {}.getClass().getEnclosingMethod().getName();

        var ret = this.databaseClient().execute(genSql(methodName, "tableName")).bind("tableName", tableName);

        return ret.as(String.class).fetch().first();
    }

    public Mono<Long> updateTableById(String tableName, String expression, Long id, Long updatedBy, Long updatedDate) {
        String methodName = new Object() {}.getClass().getEnclosingMethod().getName();

        var ret =
            this.databaseClient()
                .execute(genSql(methodName, "tableName", "expression", "id", "updatedBy", "updatedDate"))
                .bind("tableName", tableName)
                .bind("expression", expression)
                .bind("id", id)
                .bind("updatedBy", updatedBy)
                .bind("updatedDate", updatedDate);

        return ret.as(Long.class).fetch().first();
    }
}
