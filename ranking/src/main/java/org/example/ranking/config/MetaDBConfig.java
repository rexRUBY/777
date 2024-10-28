package org.example.ranking.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

@Configuration
public class MetaDBConfig {

    //2개의 db중에 metadata를 저장할 데이터를 지정해주기 충돌이 일어날수있기에 (primary)
    @Bean
    @Primary
    @ConfigurationProperties(prefix = "spring.datasource-meta")
    public DataSource metaDBSource(){
        return DataSourceBuilder.create().build();
    }

    @Primary
    @Bean
    public PlatformTransactionManager metaTransactionManager(){
        return new DataSourceTransactionManager(metaDBSource());
    }
}
