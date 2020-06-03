package vn.com.sky.task.task;

import org.springframework.stereotype.Repository;

import reactor.core.publisher.Mono;
import vn.com.sky.base.BaseR2dbcRepository;

@Repository
public class CustomTaskRepo extends BaseR2dbcRepository {

    public Mono<String> tskGetTaskById(Long id) {
        String methodName = new Object() {}.getClass().getEnclosingMethod().getName();

        var ret =
            this.databaseClient()
                .execute(genSql(methodName, "id"))
                .bind("id", id);
        
        return ret.as(String.class).fetch().first();
    }

    public Mono<String> tskFindTasks(Long userId, String menuPath, Long departmentId, Long page, Long pageSize,
    		String textSearch,
    		Boolean isExactly,
    		String taskName,
    		String projecName,
    		String assigneeName,
    		String assignerName,
    		String evaluatorName,
    		Long submitStatus,
    		Boolean isDelayDeadline,
    		Long createdDateFrom,
    		Long createdDateTo,
    		Long startTimeFrom,
    		Long startTimeTo,
    		Long deadlineFrom,
    		Long deadlineTo,
    		Boolean isAssignee,
    		Boolean isAssigner,
    		Boolean isEvaluator
    		) {
        String methodName = new Object() {}.getClass().getEnclosingMethod().getName();

        var ret =
            this.databaseClient()
                .execute(genSql(methodName, "userId", "menuPath",  "departmentId", "page", "pageSize",
                		"textSearch",
                		"isExactly",
                		"taskName",
                		"projecName",
                		"assigneeName",
                		"assignerName",
                		"evaluatorName",
                		"submitStatus",
                		"isDelayDeadline",
                		"createdDateFrom",
                		"createdDateTo",
                		"startTimeFrom",
                		"startTimeTo",
                		"deadlineFrom",
                		"deadlineTo",
                		"isAssignee",
                		"isAssigner",
                		"isEvaluator"
                		))
                .bind("userId", userId)
                .bind("isExactly", isExactly)
                .bind("menuPath", menuPath)
                .bind("departmentId", departmentId)
                .bind("page", page)
                .bind("pageSize", pageSize);
        
        
        ret = bind(ret, "textSearch", textSearch, String.class);
        ret = bind(ret, "taskName", taskName, String.class);
        ret = bind(ret, "projecName", projecName, String.class);
        ret = bind(ret, "assigneeName", assigneeName, String.class);
        ret = bind(ret, "assignerName", assignerName, String.class);
        ret = bind(ret, "evaluatorName", evaluatorName, String.class);
        ret = bind(ret, "submitStatus", submitStatus, Long.class);
        ret = bind(ret, "isDelayDeadline", isDelayDeadline, Boolean.class);
        ret = bind(ret, "createdDateFrom", createdDateFrom, Long.class);
        ret = bind(ret, "createdDateTo", createdDateTo, Long.class);
        ret = bind(ret, "startTimeFrom", startTimeFrom, Long.class);
        ret = bind(ret, "startTimeTo", startTimeTo, Long.class);
        ret = bind(ret, "deadlineFrom", deadlineFrom, Long.class);
        ret = bind(ret, "deadlineTo", deadlineTo, Long.class);
        ret = bind(ret, "isAssignee", isAssignee, Boolean.class);
        ret = bind(ret, "isAssigner", isAssigner, Boolean.class);
        ret = bind(ret, "isEvaluator", isEvaluator, Boolean.class);
               
        
        return ret.as(String.class).fetch().first();
    }
    
    
    public Mono<String> tskStatusCount(Long userId, String menuPath, Long depId) {
        String methodName = new Object() {}.getClass().getEnclosingMethod().getName();

        var ret =
            this.databaseClient()
                .execute(genSql(methodName, "userId", "menuPath", "depId"))
                .bind("userId", userId)
                .bind("menuPath", menuPath)
                .bind("depId", depId);
        
        return ret.as(String.class).fetch().first();
    }
}
