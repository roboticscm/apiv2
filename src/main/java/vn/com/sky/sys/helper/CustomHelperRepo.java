package vn.com.sky.sys.helper;

import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;
import vn.com.sky.base.BaseR2dbcRepository;

@Repository
public class CustomHelperRepo extends BaseR2dbcRepository {


    public Mono<Boolean> isManager(Long managerId, Long staffId, String menuPath, Long depId) {
        String methodName = new Object() {}.getClass().getEnclosingMethod().getName();

        var ret =
            this.databaseClient()
                .execute(genSql(methodName, "managerId", "staffId", "menuPath", "depId"))
                .bind("managerId", managerId)
                .bind("staffId", staffId)
                .bind("menuPath", menuPath)
                .bind("depId", depId);

        return ret.as(Boolean.class).fetch().first();
    }

    
}
