package vn.com.sky.base;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.r2dbc.core.DatabaseClient;
import org.springframework.data.r2dbc.core.DatabaseClient.GenericExecuteSpec;

import io.r2dbc.spi.Connection;
import io.r2dbc.spi.ConnectionFactory;
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
        String sql = "select " + storeName + "(";
        if (params.length > 0) {
            for (var i = 0; i < params.length - 1; i++) {
                sql += ":" + params[i] + ", ";
            }

            sql += ":" + params[params.length - 1] + ")";
        } else {
            sql += ")";
        }

        System.out.println(sql);
        return sql;
    }
    
    protected GenericExecuteSpec bind(GenericExecuteSpec binding, String paramName, Object value, Class<?> clazz) {
    	if(value == null)
    		binding = binding.bindNull(paramName, clazz);
    	else
    		binding = binding.bind(paramName, value);
    	
    	return binding;
    }
}
