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

    public Mono<String> tskFindTasks(Long userId, String menuPath, Long departmentId, Long page, Long pageSize) {
        String methodName = new Object() {}.getClass().getEnclosingMethod().getName();

        var ret =
            this.databaseClient()
                .execute(genSql(methodName, "userId", "menuPath",  "departmentId", "page", "pageSize"))
                .bind("userId", userId)
                .bind("menuPath", menuPath)
                .bind("departmentId", departmentId)
                .bind("page", page)
                .bind("pageSize", pageSize);
               
        
        return ret.as(String.class).fetch().first();
    }
}
