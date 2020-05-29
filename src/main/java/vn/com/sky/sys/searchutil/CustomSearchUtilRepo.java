package vn.com.sky.sys.searchutil;

import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;
import vn.com.sky.base.BaseR2dbcRepository;

@Repository
public class CustomSearchUtilRepo extends BaseR2dbcRepository {

   
    public Mono<String> findSearchFieldListByMenuPath(String menuPath) {
        String methodName = new Object() {}.getClass().getEnclosingMethod().getName();

        var ret =
            this.databaseClient()
                .execute(genSql(methodName, "menuPath"))
                .bind("menuPath", menuPath);

        return ret.as(String.class).fetch().first();
    }
}
