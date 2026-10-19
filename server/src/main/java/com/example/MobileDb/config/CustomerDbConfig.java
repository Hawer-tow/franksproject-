package com.example.MobileDb.config;

import javax.sql.DataSource;
import jakarta.persistence.EntityManagerFactory;

import org.springframework.beans.factory.annotation.Qualifier;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;

import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;


import org.springframework.context.annotation.Configuration;



@Configuration
@EnableJpaRepositories(
	    basePackages = "com.example.MobileDb.repository.customer",
	    entityManagerFactoryRef = "customerEntityManagerFactory",
	    transactionManagerRef = "customerTransactionManager"
	)

	

public class CustomerDbConfig {

    @Bean(name = "customerDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.customer")
    public DataSource customerDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean(name = "customerEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean customerEntityManagerFactory(
            EntityManagerFactoryBuilder builder,
            @Qualifier("customerDataSource") DataSource dataSource) {
        return builder
                .dataSource(dataSource)
                .packages("com.example.MobileDb.entity.customer") // Customer entities
                .persistenceUnit("customerPU")
                .build();
    }

    @Bean(name = "customerTransactionManager")
    public PlatformTransactionManager customerTransactionManager(
            @Qualifier("customerEntityManagerFactory") EntityManagerFactory emf) {
        return new JpaTransactionManager(emf);
    }
}










  

