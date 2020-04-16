package vn.com.sky;

import io.r2dbc.postgresql.PostgresqlConnectionConfiguration;
import io.r2dbc.postgresql.PostgresqlConnectionFactory;
import io.r2dbc.spi.ConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.r2dbc.config.AbstractR2dbcConfiguration;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;

/**
 * Created by IntelliJ IDEA.
 *
 * @Author: khai.lv (roboticscm2018@gmail.com)
 * Date: 3/9/2019
 * Time: 5:59 AM
 */

@Configuration
public class ConnectionFactoryConfiguration {
    @Value("${suntech.database.host}")
    private String databaseHost;

    @Value("${suntech.database.port}")
    private int databasePort;

    @Value("${suntech.database.name}")
    private String databaseName;

    @Value("${suntech.database.username}")
    private String databaseUsername;

    @Value("${suntech.database.password}")
    private String databasePassword;

    @Bean
    ConnectionFactory connectionFactory() {
        PostgresqlConnectionConfiguration config = PostgresqlConnectionConfiguration
            .builder()
            .host(databaseHost)
            .port(databasePort)
            .database(databaseName)
            .username(databaseUsername)
            .password(databasePassword)
            .build();
        return new PostgresqlConnectionFactory(config);
    }
}

@Configuration
@EnableR2dbcRepositories
class R2dbcConfiguration extends AbstractR2dbcConfiguration {
    @Autowired
    private ConnectionFactory connectionFactory;

    @Override
    public ConnectionFactory connectionFactory() {
        return connectionFactory;
    }
}
