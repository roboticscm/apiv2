package vn.com.sky.base;

import io.r2dbc.spi.Connection;
import io.r2dbc.spi.ConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.r2dbc.core.DatabaseClient;
import reactor.core.publisher.Mono;
import vn.com.sky.util.StringUtil;

public class BaseR2dbcRepository {
    @Autowired
    private ConnectionFactory connectionFactory;

    protected DatabaseClient databaseClient() {
        return DatabaseClient.create(connectionFactory);
    }

    protected Mono<Connection> connection() {
        return Mono.from(connectionFactory.create());
    }

    protected String genSql(String storeName, String... params) {
        storeName = StringUtil.toSnackCase(storeName, "_");
        System.out.println(storeName);
        String sql = "select * from " + storeName + "(";
        if (params.length > 0) {
            for (var i = 0; i < params.length - 1; i++) {
                sql += ":" + params[i] + ", ";
            }

            sql += ":" + params[params.length - 1] + ")";
        } else {
            sql += ")";
        }

        return sql;
    }
}
