package com.example.MobileDb.config;

import javax.sql.DataSource;
import jakarta.persistence.EntityManagerFactory;

import org.springframework.beans.factory.annotation.Qualifier;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
//import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager; 
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@EnableJpaRepositories(
	    basePackages = {
	    		"com.example.MobileDb.repository.kisenyi",
	    		"com.example.MobileDb.dtoRepository.kisenyi"
	    },
	    entityManagerFactoryRef = "kisenyiEntityManagerFactory",
	    transactionManagerRef = "kisenyiTransactionManager"
	)
	
	

public class KisenyiDbConfig {

    @Primary
    @Bean(name = "kisenyiDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.kisenyi")
    public DataSource kisenyiDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Primary
    @Bean(name = "kisenyiEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean kisenyiEntityManagerFactory(
            EntityManagerFactoryBuilder builder,
            @Qualifier("kisenyiDataSource") DataSource dataSource) {
        return builder
                .dataSource(dataSource)
                .packages("com.example.MobileDb.entity.kisenyi") // Kisenyi entities
                .persistenceUnit("kisenyiPU")
                .build();
    }

    @Primary
    @Bean(name = "kisenyiTransactionManager")
    public PlatformTransactionManager kisenyiTransactionManager(
            @Qualifier("kisenyiEntityManagerFactory") EntityManagerFactory emf) {
        return new JpaTransactionManager(emf);
    }
}
