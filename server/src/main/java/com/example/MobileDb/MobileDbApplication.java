package com.example.MobileDb;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class MobileDbApplication {
	
    public static void main(String[] args) {
        SpringApplication.run(MobileDbApplication.class, args);
    }
}

/*
 
 (
	    //exclude = {org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration.class}
	)
  
 */
