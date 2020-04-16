package vn.com.sky.util;

import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;
import vn.com.sky.base.BaseR2dbcRepository;

@Repository
public class CustomRepoUtil extends BaseR2dbcRepository {

    public Mono<Boolean> isTextValueExisted(String tableName, String columnName, String value) {
        String methodName = new Object() {}.getClass().getEnclosingMethod().getName();

        var ret =
            this.databaseClient()
                .execute(genSql(methodName, "tableName", "columnName", "value"))
                .bind("tableName", tableName)
                .bind("columnName", columnName);
        if (value != null) ret = ret.bind("value", value); else ret = ret.bindNull("value", String.class);

        return ret.as(Boolean.class).fetch().first();
    }

    public Mono<Boolean> isTextValueDuplicated(String tableName, String columnName, String value, Long id) {
        String methodName = new Object() {}.getClass().getEnclosingMethod().getName();

        var ret =
            this.databaseClient()
                .execute(genSql(methodName, "tableName", "columnName", "value", "id"))
                .bind("tableName", tableName)
                .bind("columnName", columnName)
                .bind("id", id);

        if (value != null) ret = ret.bind("value", value); else ret = ret.bindNull("value", String.class);

        return ret.as(Boolean.class).fetch().first();
    }
}
