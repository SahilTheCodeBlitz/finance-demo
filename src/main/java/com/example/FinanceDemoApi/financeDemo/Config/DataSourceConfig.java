package com.example.FinanceDemoApi.financeDemo.Config;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import javax.sql.DataSource;
@Configuration
public class DataSourceConfig {
    // setting the connection pool configuration
    @Bean
    public DataSource dataSource() {
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl("jdbc:mysql://localhost:3306/financedemodatabase");
        dataSource.setUsername("root");
        dataSource.setPassword("root");
        dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
        dataSource.setMinimumIdle(5);
        dataSource.setMaximumPoolSize(20);
        dataSource.setIdleTimeout(30000);
        dataSource.setMaxLifetime(1800000);
        dataSource.setConnectionTimeout(20000);
        dataSource.setPoolName("MyHikariCP");
        return dataSource;
    }
}
